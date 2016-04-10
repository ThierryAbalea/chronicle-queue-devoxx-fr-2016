package com.github.thierryabalea.ticket_sales.web.json;

import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import net.minidev.json.JSONObject;

import com.lmax.disruptor.EventTranslator;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;

public class TicketPurchaseFromJson implements EventTranslator<Message>
{
    private final JSONObject object;

    public TicketPurchaseFromJson(JSONObject object)
    {
        this.object = object;
    }

    public Message translateTo(Message message, long sequence)
    {
        message.type.set(EventType.TICKET_PURCHASE);
        
        Number concertId = (Number) object.get("concertId");
        Number sectionId = (Number) object.get("sectionId");
        Number numSeats = (Number) object.get("numSeats");
        Number accountId = (Number) object.get("accountId");
        Number requestId = (Number) object.get("requestId");
        
        TicketPurchase ticketPurchase = message.event.asTicketPurchase;
        
        ticketPurchase.concertId.set(concertId.longValue());
        ticketPurchase.sectionId.set(sectionId.longValue());
        ticketPurchase.numSeats.set(numSeats.intValue());
        ticketPurchase.accountId.set(accountId.longValue());
        ticketPurchase.requestId.set(requestId.longValue());
        
        return message;
    }
}
