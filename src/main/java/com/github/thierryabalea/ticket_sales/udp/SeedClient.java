package com.github.thierryabalea.ticket_sales.udp;

import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.SectionSeating;

import java.util.Arrays;

public class SeedClient {
    public static void main(String[] args) throws Exception {
        UdpEventHandler udpEventHandler = new UdpEventHandler("localhost", ConcertServiceMain.SERVER_PORT);
        {
            long concertId = 1L;
            ConcertCreated concertCreated = new ConcertCreated(
                    concertId,
                    0,
                    "Red Hot Chili Peppers",
                    "Albert Hall",
                    (short) 8,
                    Arrays.asList(
                            new SectionSeating(1, "Section A", 58.50F, Integer.MAX_VALUE),
                            new SectionSeating(2, "Section B", 63.50F, Integer.MAX_VALUE),
                            new SectionSeating(3, "Section C", 45.50F, Integer.MAX_VALUE),
                            new SectionSeating(4, "Section D", 67.50F, Integer.MAX_VALUE),
                            new SectionSeating(5, "Section E", 38.00F, Integer.MAX_VALUE),
                            new SectionSeating(6, "Section F", 31.95F, Integer.MAX_VALUE),
                            new SectionSeating(7, "Section G", 58.50F, Integer.MAX_VALUE),
                            new SectionSeating(8, "Section H", 78.89F, Integer.MAX_VALUE))
            );

            Message m1 = new Message();
            m1.type = EventType.CONCERT_CREATED;
            m1.event = concertCreated;
            udpEventHandler.onEvent(m1, 0, true);
        }

        {
            long concertId = 2L;
            ConcertCreated concertCreated = new ConcertCreated(
                    concertId,
                    0,
                    "Gomez",
                    "Wembley Park",
                    (short) 8,
                    Arrays.asList(
                            new SectionSeating(1, "Section A", 58.50F, Integer.MAX_VALUE),
                            new SectionSeating(2, "Section B", 63.50F, Integer.MAX_VALUE),
                            new SectionSeating(3, "Section C", 45.50F, Integer.MAX_VALUE),
                            new SectionSeating(4, "Section D", 67.50F, Integer.MAX_VALUE),
                            new SectionSeating(5, "Section E", 38.00F, Integer.MAX_VALUE),
                            new SectionSeating(6, "Section F", 66.55F, Integer.MAX_VALUE),
                            new SectionSeating(7, "Section G", 31.95F, Integer.MAX_VALUE),
                            new SectionSeating(8, "Section H", 78.89F, Integer.MAX_VALUE)
                    )
            );

            Message m1 = new Message();
            m1.event =  concertCreated;
            m1.type = EventType.CONCERT_CREATED;
            udpEventHandler.onEvent(m1, 0, true);
        }
    }
}
