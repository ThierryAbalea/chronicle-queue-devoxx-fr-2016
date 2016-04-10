package com.github.thierryabalea.ticket_sales.web;

import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.io.UdpEventHandler;
import com.github.thierryabalea.ticket_sales.web.json.TicketPurchaseFromJson;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.rapidoid.http.Req;
import org.rapidoid.http.fast.On;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestServer.class);
    private static final String REQUEST_UDP_HOST = "localhost";
    private static final int REQUEST_UDP_PORT = 50001;
    private RingBuffer<Message> ringBuffer;

    public void init() {
        String host = REQUEST_UDP_HOST;
        int port = REQUEST_UDP_PORT;

        LOGGER.info("Connect to {}:{}", host, port);

        Disruptor<Message> disruptor = new Disruptor<>(
                Message.FACTORY,
                1024,
                DaemonThreadFactory.INSTANCE);
        UdpEventHandler handler = new UdpEventHandler(host, port);
        disruptor.handleEventsWith(handler);

        ringBuffer = disruptor.start();

        On.post("/request").plain((Req req) -> {
            doPost(req);
            return "OK";
        });
    }

    private void doPost(Req req) throws Exception {
        JSONParser parser = new JSONParser(JSONParser.MODE_RFC4627);
        String stringRequest = new String(req.body());
        JSONObject request = (JSONObject) parser.parse(stringRequest);
        ringBuffer.publishEvent(TicketPurchaseFromJson::translateTo, request);
    }
}
