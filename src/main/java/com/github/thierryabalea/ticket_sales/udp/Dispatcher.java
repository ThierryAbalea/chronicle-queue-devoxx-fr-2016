package com.github.thierryabalea.ticket_sales.udp;

import com.github.thierryabalea.ticket_sales.api.CreateConcert;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;

public class Dispatcher {
    private final ConcertService service;

    public Dispatcher(ConcertService service) {
        this.service = service;
    }

    public void onEvent(Message message, long sequence, boolean endOfBatch) {
        EventType type = message.type;

        switch (type) {
            case CREATE_CONCERT:
                service.onCreateConcert((CreateConcert) message.event);
                break;

            case TICKET_PURCHASE:
                service.onTicketPurchase((TicketPurchase) message.event);
                break;
        }
    }
}
