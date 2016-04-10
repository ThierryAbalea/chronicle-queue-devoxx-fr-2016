package com.github.thierryabalea.ticket_sales.web;

import com.github.thierryabalea.ticket_sales.web.json.TicketPurchaseFromJson;
import com.lmax.disruptor.EventPublisher;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.io.UdpEventHandler;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.rapidoid.http.Req;
import org.rapidoid.http.fast.On;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class RequestServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestServer.class);
    private static final String REQUEST_UDP_HOST = "localhost";
    private static final int REQUEST_UDP_PORT = 50001;
    private EventPublisher<Message> eventPublisher;

    public void init() {
        String host = REQUEST_UDP_HOST;
        int port = REQUEST_UDP_PORT;

        LOGGER.info("Connect to {}:{}", host, port);

        Disruptor<Message> disruptor = new Disruptor<Message>(Message.FACTORY, 1024, newSingleThreadExecutor());
        UdpEventHandler handler = new UdpEventHandler(host, port);
        disruptor.handleEventsWith(handler);

        RingBuffer<Message> ringBuffer = disruptor.start();
        eventPublisher = new EventPublisher<Message>(ringBuffer);

        On.post("/request").plain((Req req) -> {
            doPost(req);
            return "OK";
        });
    }

    private void doPost(Req req) throws Exception
    {
        JSONParser parser = new JSONParser(JSONParser.MODE_RFC4627);
        String stringRequest = new String(req.body());
        JSONObject request = (JSONObject) parser.parse(stringRequest);
        eventPublisher.publishEvent(new TicketPurchaseFromJson(request));
    }
}
