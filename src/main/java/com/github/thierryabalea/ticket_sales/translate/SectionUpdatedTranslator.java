package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.SectionUpdated;

public class SectionUpdatedTranslator {

    public static void translateTo(Message message, long sequence, long concertId, long sectionId, int seatsAvailable) {

        SectionUpdated sectionUpdated = new SectionUpdated(
                concertId,
                sectionId,
                sequence,
                seatsAvailable
        );
        message.type = EventType.SECTION_UPDATED;
        message.event = sectionUpdated;

    }
}
