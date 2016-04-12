package com.github.thierryabalea.ticket_sales.udp;

import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import com.github.thierryabalea.ticket_sales.domain.ConcertServiceManager;

public class Dispatcher {
    private final ConcertServiceManager service;

    public Dispatcher(ConcertServiceManager service) {
        this.service = service;
    }

    public void onEvent(Message message, long sequence, boolean endOfBatch) {
        EventType type = message.type;

        switch (type) {
            case CONCERT_CREATED:
                service.onConcertCreated((ConcertCreated) message.event);
                break;

            case TICKET_PURCHASE:
                service.onTicketPurchase((TicketPurchase) message.event);
                break;
        }
    }
}
