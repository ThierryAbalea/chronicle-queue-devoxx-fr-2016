package com.github.thierryabalea.ticket_sales.udp;

import com.github.thierryabalea.ticket_sales.CreateConcertsFactory;
import com.github.thierryabalea.ticket_sales.api.EventType;

import static com.google.common.base.Throwables.propagate;

public class SeedClient {
    public static void main(String[] args) throws Exception {

        CreateConcertsFactory.createConcerts().stream().forEachOrdered(concertCreated -> {
            Message message = new Message();
            message.type = EventType.CONCERT_CREATED;
            message.event = concertCreated;
            handleEvent(message);
        });
    }

    private static void handleEvent(Message message) {
        UdpEventHandler udpEventHandler = new UdpEventHandler("localhost", UDPConcertServiceMain.SERVER_PORT);
        try {
            udpEventHandler.onEvent(message, 0, true);
        } catch (Exception e) {
            throw propagate(e);
        }
    }
}
