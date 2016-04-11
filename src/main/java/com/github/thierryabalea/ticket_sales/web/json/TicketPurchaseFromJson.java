package com.github.thierryabalea.ticket_sales.web.json;

import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import net.minidev.json.JSONObject;

public class TicketPurchaseFromJson {
    public static void translateTo(Message message, long sequence, JSONObject object) {
        Number concertId = (Number) object.get("concertId");
        Number sectionId = (Number) object.get("sectionId");
        Number numSeats = (Number) object.get("numSeats");
        Number accountId = (Number) object.get("accountId");
        Number requestId = (Number) object.get("requestId");

        TicketPurchase ticketPurchase = new TicketPurchase(
                concertId.longValue(),
                sectionId.longValue(),
                numSeats.intValue(),
                accountId.longValue(),
                requestId.longValue()
        );
        message.type =  EventType.TICKET_PURCHASE;
        message.event= ticketPurchase;
    }
}
