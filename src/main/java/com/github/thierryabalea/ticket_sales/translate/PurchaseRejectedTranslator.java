package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.*;

public class PurchaseRejectedTranslator {

    public static void translateTo(Message message, long sequence, RejectionReason rejectionReason, TicketPurchase ticketPurchase) {
        AllocationRejected allocationRejected = new AllocationRejected(
                ticketPurchase.accountId,
                ticketPurchase.requestId,
                rejectionReason
        );
        message.type = EventType.ALLOCATION_REJECTED;
        message.event = allocationRejected;

    }
}
