package com.github.thierryabalea.ticket_sales.udp.translate;

import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.udp.EventType;
import com.github.thierryabalea.ticket_sales.udp.Message;

public class ConcertCreatedTranslator {

    public static void translateTo(Message message, long sequence, ConcertCreated concertCreated) {
        message.type = EventType.CONCERT_CREATED;
        message.event = concertCreated;

    }
}
