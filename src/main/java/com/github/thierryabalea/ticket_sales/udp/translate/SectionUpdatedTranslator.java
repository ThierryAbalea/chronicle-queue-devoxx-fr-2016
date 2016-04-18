package com.github.thierryabalea.ticket_sales.udp.translate;

import com.github.thierryabalea.ticket_sales.api.event.SectionUpdated;
import com.github.thierryabalea.ticket_sales.udp.EventType;
import com.github.thierryabalea.ticket_sales.udp.Message;

public class SectionUpdatedTranslator {

    public static void translateTo(Message message, long sequence, SectionUpdated sectionUpdated) {
        message.type = EventType.SECTION_UPDATED;
        // the following event will be passed to another thread via the disruptor and
        // the parameter instance will be mutated by the current thread
        message.event = new SectionUpdated()
                .init(sectionUpdated.concertId, sectionUpdated.sectionId, sectionUpdated.version, sectionUpdated.seatsAvailable);
        message.event = sectionUpdated;
    }
}
