package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;

public class PurchaseApprovedTranslator {
    public static void translateTo(Message message, long sequence, TicketPurchase ticketPurchase) {

        AllocationApproved allocationApproved = new AllocationApproved(
                ticketPurchase.accountId,
                ticketPurchase.requestId,
                ticketPurchase.numSeats
        );

        message.type = EventType.ALLOCATION_APPROVED;
        message.event = allocationApproved;

    }
}
