package com.github.thierryabalea.ticket_sales.udp.translate;

import com.github.thierryabalea.ticket_sales.api.event.AllocationApproved;
import com.github.thierryabalea.ticket_sales.udp.EventType;
import com.github.thierryabalea.ticket_sales.udp.Message;

public class PurchaseApprovedTranslator {
    public static void translateTo(Message message, long sequence, AllocationApproved allocationApproved) {
        message.type = EventType.ALLOCATION_APPROVED;
        // the following event will be passed to another thread via the disruptor and
        // the parameter instance will be mutated by the current thread
        message.event = new AllocationApproved()
                .init(allocationApproved.accountId, allocationApproved.requestId, allocationApproved.numSeats);
    }
}
