package com.github.thierryabalea.ticket_sales.udp;

import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.Poll;
import com.github.thierryabalea.ticket_sales.web.RequestWebServer;
import com.github.thierryabalea.ticket_sales.web.ResponseWebServer;
import com.github.thierryabalea.ticket_sales.web.json.TicketPurchaseFromJson;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
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
                ringBuffer.publishEvent(TicketPurchaseFromJson::translateTo, request);
        RequestWebServer requestWebServer =
                new RequestWebServer(requestHandler);

        requestWebServer.init();
    }

    private static void startResponseDisruptorAndWebServer() throws Exception {
        int port = RESPONSE_UDP_PORT;

        Disruptor<Message> disruptor = new Disruptor<>(
                Message.FACTORY,
                1024,
                DaemonThreadFactory.INSTANCE);
        ResponseWebServer responseWebServer = new ResponseWebServer();
        disruptor.handleEventsWith(responseWebServer::onEvent);
        RingBuffer<Message> ringBuffer = disruptor.start();

        UdpDataSource udpDataSource = new UdpDataSource(ringBuffer, port);
        udpDataSource.bind();

        executor.execute(udpDataSource);

        LOGGER.info("Listening on {}", port);

        ResponseWebServer.PollHandler pollHandler = (accountId, version) -> {
            long next = ringBuffer.next();
            Message message = ringBuffer.get(next);
            message.type = EventType.POLL;
            message.event = new Poll(accountId, version);
            ringBuffer.publish(next);
        };

        responseWebServer.init(pollHandler);
    }
}
