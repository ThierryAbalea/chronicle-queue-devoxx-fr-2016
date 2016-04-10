package com.github.thierryabalea.ticket_sales.web;

import com.github.thierryabalea.ticket_sales.api.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.SectionUpdated;
import com.github.thierryabalea.ticket_sales.io.UdpDataSource;
import com.github.thierryabalea.ticket_sales.web.json.AllocationApprovedToJson;
import com.github.thierryabalea.ticket_sales.web.json.AllocationRejectedToJson;
import com.github.thierryabalea.ticket_sales.web.json.ConcertCreatedToJson;
import com.github.thierryabalea.ticket_sales.web.json.SectionUpdatedToJson;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.rapidoid.commons.MediaType;
import org.rapidoid.http.Req;
import org.rapidoid.http.fast.On;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ResponseServer implements EventHandler<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseServer.class);
    private static final int RESPONSE_UDP_PORT = 50002;
    private final Executor executor = Executors.newCachedThreadPool();
    private RingBuffer<Message> ringBuffer;

    // Event data
    private final Long2ObjectMap<JSONArray> eventsByAccountId = new Long2ObjectOpenHashMap<JSONArray>();
    private final Long2ObjectMap<JSONObject> concertsByConcertId = new Long2ObjectOpenHashMap<JSONObject>();
    private final Map<SectionKey, JSONObject> sectionUpdatedByKey = new HashMap<SectionKey, JSONObject>();

    // Translators
    private final ConcertCreatedToJson concertCreatedToJson = new ConcertCreatedToJson();
    private final SectionUpdatedToJson sectionUpdatedToJson = new SectionUpdatedToJson();
    private final AllocationApprovedToJson allocationApprovedToJson = new AllocationApprovedToJson();
    private final AllocationRejectedToJson allocationRejectedToJson = new AllocationRejectedToJson();

    // Current contexts
    private final ConcurrentMap<Long, Req> requestsByAccount = new ConcurrentHashMap<Long, Req>();

    public void init() throws IOException {
        int port = RESPONSE_UDP_PORT;

        Disruptor<Message> disruptor = new Disruptor<>(
                Message.FACTORY,
                1024,
                DaemonThreadFactory.INSTANCE);

        disruptor.handleEventsWith(this);
        ringBuffer = disruptor.start();

        UdpDataSource udpDataSource = new UdpDataSource(ringBuffer, port);
        udpDataSource.bind();
        executor.execute(udpDataSource);

        LOGGER.info("Listening on {}", port);

        On.post("/response").plain((Req req) -> {
            doPost(req);
            return "OK";
        });
    }

    @Override
    public void onEvent(Message message, long sequence, boolean endOfBatch) throws Exception {
        EventType type = (EventType) message.type.get();

        switch (type) {
            case CONCERT_CREATED:
                final ConcertCreated concertCreated = message.event.asConcertCreated;
                JSONObject concertCreatedAsJson = concertCreatedToJson.toJson(concertCreated);
                concertsByConcertId.put(concertCreated.concertId.get(), concertCreatedAsJson);
                enqueueEvent(concertCreatedAsJson);
                break;

            case SECTION_UPDATED:
                final SectionUpdated sectionUpdated = message.event.asSectionUpdated;
                JSONObject sectionUpdatedAsJson = sectionUpdatedToJson.toJson(sectionUpdated);
                sectionUpdatedByKey.put(sectionKeyFrom(sectionUpdated), sectionUpdatedAsJson);
                enqueueEvent(sectionUpdatedAsJson);
                break;

            case ALLOCATION_APPROVED:
                final AllocationApproved approval = message.event.asAllocationApproved;
                enqueueEvent(approval.accountId.get(), allocationApprovedToJson.toJson(approval));
                break;

            case ALLOCATION_REJECTED:
                final AllocationRejected rejection = message.event.asAllocationRejected;
                enqueueEvent(rejection.accountId.get(), allocationRejectedToJson.toJson(rejection));
                break;

            case POLL:
                long accountId = message.event.asPoll.accountId.get();
                long version = message.event.asPoll.version.get();
                JSONArray events = eventsByAccountId.get(accountId);

                events = getUpdatedValues(events, concertsByConcertId.values(), version);
                events = getUpdatedValues(events, sectionUpdatedByKey.values(), version);

                if (null != events && !events.isEmpty()) {
                    dispatch(accountId, events);
                }
                break;
        }
    }

    private JSONArray getUpdatedValues(JSONArray events, Collection<JSONObject> values, long version) {
        for (JSONObject value : values) {
            long valueVersion = (Long) value.get("version");
            if (version < valueVersion) {
                if (null == events) {
                    events = new JSONArray();
                }

                events.add(value);
            }
        }
        return events;
    }

    private void enqueueEvent(JSONObject jsonEvent) {
        for (Long accountId : requestsByAccount.keySet()) {
            JSONArray events = getEventsForAccount(accountId);
            events.add(jsonEvent);

            dispatch(accountId, events);
        }
    }

    private void enqueueEvent(long accountId, JSONObject jsonEvent) {
        JSONArray events = getEventsForAccount(accountId);
        events.add(jsonEvent);

        dispatch(accountId, events);
    }

    private void dispatch(long accountId, JSONArray events) {
        Req req = requestsByAccount.remove(accountId);

        if (null != req) {
            StringBuilder jsonResponse = new StringBuilder();
            try {
                JSONValue.writeJSONString(events, jsonResponse);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            req.response().contentType(MediaType.JSON_UTF_8).content(jsonResponse);
            req.done();

            eventsByAccountId.remove(accountId);
        }
    }

    private JSONArray getEventsForAccount(long accountId) {
        JSONArray queue = eventsByAccountId.get(accountId);
        if (queue == null) {
            queue = new JSONArray();
            eventsByAccountId.put(accountId, queue);
        }

        return queue;
    }

    private void doPost(Req req) {
        long accountId = Long.parseLong(req.param("account"));
        long version = Long.parseLong(req.param("version"));
        req.async();
        requestsByAccount.put(accountId, req);

        long next = ringBuffer.next();
        Message message = ringBuffer.get(next);
        message.type.set(EventType.POLL);
        message.event.asPoll.accountId.set(accountId);
        message.event.asPoll.version.set(version);
        ringBuffer.publish(next);
    }

    private SectionKey sectionKeyFrom(final SectionUpdated sectionUpdated) {
        return new SectionKey(sectionUpdated.concertId.get(), sectionUpdated.sectionId.get());
    }

    private static class SectionKey {
        private final long concertId;
        private final long sectionId;

        public SectionKey(long concertId, long sectionId) {
            this.concertId = concertId;
            this.sectionId = sectionId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (concertId ^ (concertId >>> 32));
            result = prime * result + (int) (sectionId ^ (sectionId >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SectionKey other = (SectionKey) obj;
            if (concertId != other.concertId)
                return false;
            if (sectionId != other.sectionId)
                return false;
            return true;
        }
    }
}
