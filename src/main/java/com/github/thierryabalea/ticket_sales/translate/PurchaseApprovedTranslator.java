package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import com.lmax.disruptor.EventTranslator;
import com.github.thierryabalea.ticket_sales.api.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;

public class PurchaseApprovedTranslator implements EventTranslator<Message>
{
    private TicketPurchase ticketPurchase;

    public Message translateTo(Message message, long sequence)
    {
        message.type.set(EventType.ALLOCATION_APPROVED);
        AllocationApproved allocationApproved = message.event.asAllocationApproved;
        
        allocationApproved.accountId.set(ticketPurchase.accountId.get());
        allocationApproved.requestId.set(ticketPurchase.requestId.get());
        allocationApproved.numSeats.set(ticketPurchase.numSeats.get());
        
        return message;
    }

    public void set(TicketPurchase ticketPurchase)
    {
        this.ticketPurchase = ticketPurchase;
    }
}
