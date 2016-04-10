package com.github.thierryabalea.ticket_sales.framework;

import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;

public class Dispatcher
{
    private final ConcertService service;

    public Dispatcher(ConcertService service)
    {
        this.service = service;
    }
    
    public void onEvent(Message message, long sequence, boolean endOfBatch)
    {
        EventType type = (EventType) message.type.get();
        
        switch (type)
        {
        case CONCERT_CREATED:
            service.on(message.event.asConcertCreated);
            break;
            
        case TICKET_PURCHASE:
            service.on(message.event.asTicketPurchase);
            break;
        }
    }
}
