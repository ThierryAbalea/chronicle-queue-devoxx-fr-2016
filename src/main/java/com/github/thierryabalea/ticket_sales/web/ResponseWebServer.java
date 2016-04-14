package com.github.thierryabalea.ticket_sales.web;

import com.github.thierryabalea.ticket_sales.api.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.SectionUpdated;
import com.github.thierryabalea.ticket_sales.domain.ConcertServiceListener;
import com.github.thierryabalea.ticket_sales.web.json.AllocationApprovedToJson;
import com.github.thierryabalea.ticket_sales.web.json.AllocationRejectedToJson;
import com.github.thierryabalea.ticket_sales.web.json.ConcertCreatedToJson;
import com.github.thierryabalea.ticket_sales.web.json.SectionUpdatedToJson;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.rapidoid.http.Req;
import org.rapidoid.http.fast.On;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ResponseWebServer implements ConcertServiceListener {

    public interface PollHandler {
        void onPoll(long accountId, long version);
    }

    // Event data
    private final Long2ObjectMap<JSONArray> eventsByAccountId = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<JSONObject> concertsByConcertId = new Long2ObjectOpenHashMap<>();
    private final Map<SectionKey, JSONObject> sectionUpdatedByKey = new HashMap<>();

    // Translators
    private final ConcertCreatedToJson concertCreatedToJson = new ConcertCreatedToJson();
    private final SectionUpdatedToJson sectionUpdatedToJson = new SectionUpdatedToJson();
    private final AllocationApprovedToJson allocationApprovedToJson = new AllocationApprovedToJson();
    private final AllocationRejectedToJson allocationRejectedToJson = new AllocationRejectedToJson();

    // Current contexts
    private final ConcurrentMap<Long, Req> requestsByAccount = new ConcurrentHashMap<>();

    private PollHandler pollHandler;

    public void init(PollHandler pollHandler) throws IOException {
        this.pollHandler = pollHandler;

        On.post("/response").plain((Req req) -> {
            doPost(req);
            return "OK";
        });
    }

    @Override
    public void onConcertAvailable(ConcertCreated concertCreated) {
        JSONObject concertCreatedAsJson = concertCreatedToJson.toJson(concertCreated);
        concertsByConcertId.put(concertCreated.concertId, concertCreatedAsJson);
        enqueueEvent(concertCreatedAsJson);
    }

    @Override
    public void onSectionUpdated(SectionUpdated sectionUpdated) {
        JSONObject sectionUpdatedAsJson = sectionUpdatedToJson.toJson(sectionUpdated);
        sectionUpdatedByKey.put(sectionKeyFrom(sectionUpdated), sectionUpdatedAsJson);
        enqueueEvent(sectionUpdatedAsJson);
    }

    @Override
    public void onAllocationApproved(AllocationApproved allocationApproved) {
        enqueueEvent(allocationApproved.accountId, allocationApprovedToJson.toJson(allocationApproved));
    }

    @Override
    public void onAllocationRejected(AllocationRejected allocationRejected) {
        enqueueEvent(allocationRejected.accountId, allocationRejectedToJson.toJson(allocationRejected));
    }

    public void onPoll(long accountId, long version) {
        JSONArray events = eventsByAccountId.get(accountId);

        events = getUpdatedValues(events, concertsByConcertId.values(), version);
        events = getUpdatedValues(events, sectionUpdatedByKey.values(), version);

        if (null != events && !events.isEmpty()) {
            dispatch(accountId, events);
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
            req.response().json(events);
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

        pollHandler.onPoll(accountId, version);
    }

    private SectionKey sectionKeyFrom(final SectionUpdated sectionUpdated) {
        return new SectionKey(sectionUpdated.concertId, sectionUpdated.sectionId);
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
