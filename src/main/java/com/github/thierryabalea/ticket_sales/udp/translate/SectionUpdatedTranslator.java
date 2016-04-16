package com.github.thierryabalea.ticket_sales.udp.translate;

import com.github.thierryabalea.ticket_sales.udp.EventType;
import com.github.thierryabalea.ticket_sales.udp.Message;
import com.github.thierryabalea.ticket_sales.api.SectionUpdated;

public class SectionUpdatedTranslator {

    public static void translateTo(Message message, long sequence, SectionUpdated sectionUpdated) {
        message.type = EventType.SECTION_UPDATED;
        message.event = sectionUpdated;
    }
}
