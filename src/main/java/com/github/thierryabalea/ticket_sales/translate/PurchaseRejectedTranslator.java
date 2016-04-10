package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import com.lmax.disruptor.EventTranslator;
import com.github.thierryabalea.ticket_sales.api.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.RejectionReason;

public class PurchaseRejectedTranslator implements EventTranslator<Message>
{
    private RejectionReason rejectionReason;
    private TicketPurchase ticketPurchase;

    public Message translateTo(Message message, long sequence)
    {
        message.type.set(EventType.ALLOCATION_REJECTED);
        
        AllocationRejected allocationRejected = message.event.asAllocationRejected;
        allocationRejected.accountId.set(ticketPurchase.accountId.get());
        allocationRejected.requestId.set(ticketPurchase.requestId.get());
        allocationRejected.reason.set(rejectionReason);
        
        return message;
    }

    public void set(RejectionReason rejectionReason, TicketPurchase ticketPurchase)
    {
        this.rejectionReason = rejectionReason;
        this.ticketPurchase = ticketPurchase;
    }
}
