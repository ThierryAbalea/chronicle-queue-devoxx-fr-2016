package com.github.thierryabalea.ticket_sales.udp;

import com.github.thierryabalea.ticket_sales.api.command.Poll;
import com.github.thierryabalea.ticket_sales.api.event.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.event.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.event.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.event.SectionUpdated;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
import com.github.thierryabalea.ticket_sales.udp.translate.PollTranslator;
import com.github.thierryabalea.ticket_sales.web.RequestWebServer;
import com.github.thierryabalea.ticket_sales.web.ResponseWebServer;
import com.github.thierryabalea.ticket_sales.web.json.TicketPurchaseFromJson;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import net.minidev.json.JSONObject;
import org.rapidoid.http.fast.On;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UdpWebMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpWebMain.class);
    private static final String REQUEST_UDP_HOST = "localhost";
    private static final int REQUEST_UDP_PORT = 50001;
    private static final int RESPONSE_UDP_PORT = 50002;
    private static Executor executor = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception {
        On.port(7070);

        startRequestDisruptorAndWebServer();
        startResponseDisruptorAndWebServer();
    }

    private static void startRequestDisruptorAndWebServer() throws Exception {
        String host = REQUEST_UDP_HOST;
        int port = REQUEST_UDP_PORT;

        LOGGER.info("Connect to {}:{}", host, port);

        Disruptor<Message> disruptor = new Disruptor<>(
                Message.FACTORY,
                1024,
                DaemonThreadFactory.INSTANCE);
        UdpEventHandler handler = new UdpEventHandler(host, port);
        disruptor.handleEventsWith(handler);

        RingBuffer<Message> ringBuffer = disruptor.start();

        RequestWebServer.JsonRequestHandler requestHandler = request ->
                ringBuffer.publishEvent(UdpWebMain::translateTo, request);
        RequestWebServer requestWebServer =
                new RequestWebServer(requestHandler);

        requestWebServer.init();
    }

    private static void translateTo(Message message, long sequence, JSONObject object) {
        message.type = EventType.TICKET_PURCHASE;
        message.event = TicketPurchaseFromJson.fromJson(object);
    }

    private static void startResponseDisruptorAndWebServer() throws Exception {
        int port = RESPONSE_UDP_PORT;

        Disruptor<Message> disruptor = new Disruptor<>(
                Message.FACTORY,
                1024,
                DaemonThreadFactory.INSTANCE);
        ResponseWebServer responseWebServer = new ResponseWebServer();
        EventProcessor eventProcessor = new EventProcessor(responseWebServer, responseWebServer::onPoll);
        disruptor.handleEventsWith(eventProcessor::onEvent);
        RingBuffer<Message> ringBuffer = disruptor.start();

        UdpDataSource udpDataSource = new UdpDataSource(ringBuffer, port);
        udpDataSource.bind();

        executor.execute(udpDataSource);

        LOGGER.info("Listening onConcertCreated {}", port);

        ResponseWebServer.PollHandler pollHandler =
                (accountId, version) -> ringBuffer.publishEvent(PollTranslator::translateTo, accountId, version);
        responseWebServer.init(pollHandler);
    }

    public static class EventProcessor {
        private EventHandler eventHandler;
        private ResponseWebServer.PollHandler pollHandler;

        public EventProcessor(EventHandler eventHandler, ResponseWebServer.PollHandler pollHandler) {
            this.eventHandler = eventHandler;
            this.pollHandler = pollHandler;
        }

        public void onEvent(Message message, long sequence, boolean endOfBatch) throws Exception {
            EventType type = message.type;

            switch (type) {
                case CONCERT_CREATED:
                    final ConcertCreated concertCreated = (ConcertCreated) message.event;
                    eventHandler.onConcertAvailable(concertCreated);
                    break;

                case SECTION_UPDATED:
                    final SectionUpdated sectionUpdated = (SectionUpdated) message.event;
                    eventHandler.onSectionUpdated(sectionUpdated);
                    break;

                case ALLOCATION_APPROVED:
                    final AllocationApproved allocationApproved = (AllocationApproved) message.event;
                    eventHandler.onAllocationApproved(allocationApproved);
                    break;

                case ALLOCATION_REJECTED:
                    final AllocationRejected rejection = (AllocationRejected) message.event;
                    eventHandler.onAllocationRejected(rejection);
                    break;

                case POLL:
                    final Poll poll = (Poll) message.event;
                    pollHandler.onPoll(poll.accountId, poll.version);
                    break;
            }
        }
    }
}