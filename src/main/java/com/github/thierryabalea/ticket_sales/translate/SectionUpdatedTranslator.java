package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.SectionUpdated;

public class SectionUpdatedTranslator {
    public static void translateTo(Message message, long sequence, long concertId, long sectionId, int seatsAvailable) {
        message.type.set(EventType.SECTION_UPDATED);

        SectionUpdated sectionUpdated = message.event.asSectionUpdated;

        sectionUpdated.concertId.set(concertId);
        sectionUpdated.sectionId.set(sectionId);
        sectionUpdated.version.set(sequence);
        sectionUpdated.seatsAvailable.set(seatsAvailable);
    }
}
