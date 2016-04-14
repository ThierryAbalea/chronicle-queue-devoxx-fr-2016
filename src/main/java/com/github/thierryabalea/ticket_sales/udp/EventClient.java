package com.github.thierryabalea.ticket_sales.udp;

import com.github.thierryabalea.ticket_sales.api.*;

import java.util.Arrays;

public class EventClient {
    public static void main(String[] args) throws Exception {
        UdpEventHandler udpEventHandler = new UdpEventHandler("localhost", UDPConcertServiceMain.SERVER_PORT);
        long concertId = System.currentTimeMillis();

        CreateConcert createConcert = new CreateConcert(
                concertId,
                0,
                "Chilis",
                "Albert Hall",
                (short) 1,
                Arrays.asList(new SectionSeating(456, "Wing 5", 13.50F, Integer.MAX_VALUE))
        );

        Message m1 = new Message();
        m1.type = EventType.CONCERT_CREATED;
        m1.event = createConcert;
        udpEventHandler.onEvent(m1, 0, true);

        for (int i = 0; i < 1; i++) {
            TicketPurchase ticketPurchase = new TicketPurchase(
                    concertId,
                    456,
                    1,
                    1001,
                    2002 + i
            );


            Message m2 = new Message();
            m2.event = ticketPurchase;
            m2.type = EventType.TICKET_PURCHASE;
            udpEventHandler.onEvent(m2, 0, true);
        }
    }
}
