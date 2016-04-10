package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;

public class PurchaseApprovedTranslator {
    public static void translateTo(Message message, long sequence, TicketPurchase ticketPurchase) {
        message.type.set(EventType.ALLOCATION_APPROVED);
        AllocationApproved allocationApproved = message.event.asAllocationApproved;

        allocationApproved.accountId.set(ticketPurchase.accountId.get());
        allocationApproved.requestId.set(ticketPurchase.requestId.get());
        allocationApproved.numSeats.set(ticketPurchase.numSeats.get());
    }
}
