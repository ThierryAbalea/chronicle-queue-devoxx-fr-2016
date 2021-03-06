package com.github.thierryabalea.ticket_sales.udp;

import static com.github.thierryabalea.ticket_sales.ConcertFactory.createConcerts;
import static com.google.common.base.Throwables.propagate;

public class SeedClient {
    public static void main(String[] args) throws Exception {

        createConcerts().stream().forEachOrdered(createConcert -> {
            Message message = new Message();
            message.type = EventType.CREATE_CONCERT;
            message.event = createConcert;
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
