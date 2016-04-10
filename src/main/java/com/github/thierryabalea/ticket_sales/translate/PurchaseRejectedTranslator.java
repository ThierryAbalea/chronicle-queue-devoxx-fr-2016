package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.RejectionReason;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;

public class PurchaseRejectedTranslator {
    public static void translateTo(Message message, long sequence, RejectionReason rejectionReason,
                            TicketPurchase ticketPurchase) {
        message.type.set(EventType.ALLOCATION_REJECTED);

        AllocationRejected allocationRejected = message.event.asAllocationRejected;
        allocationRejected.accountId.set(ticketPurchase.accountId.get());
        allocationRejected.requestId.set(ticketPurchase.requestId.get());
        allocationRejected.reason.set(rejectionReason);
    }
}
