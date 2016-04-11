package com.github.thierryabalea.ticket_sales.udp.translate;

import com.github.thierryabalea.ticket_sales.api.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.udp.Message;

public class PurchaseApprovedTranslator {
    public static void translateTo(Message message, long sequence, AllocationApproved allocationApproved) {
        message.type = EventType.ALLOCATION_APPROVED;
        message.event = allocationApproved;
    }
}
