package com.github.thierryabalea.ticket_sales.udp.translate;

import com.github.thierryabalea.ticket_sales.api.event.AllocationRejected;
import com.github.thierryabalea.ticket_sales.udp.EventType;
import com.github.thierryabalea.ticket_sales.udp.Message;

public class AllocationRejectedTranslator {

    public static void translateTo(Message message, long sequence, AllocationRejected allocationRejected) {
        message.type = EventType.ALLOCATION_REJECTED;
        message.event = allocationRejected;
    }
}
