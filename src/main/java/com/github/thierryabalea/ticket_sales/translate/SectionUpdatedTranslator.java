package com.github.thierryabalea.ticket_sales.translate;

import com.lmax.disruptor.EventTranslator;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.SectionUpdated;

public class SectionUpdatedTranslator implements EventTranslator<Message>
{
    private long concertId;
    private long sectionId;
    private int seatsAvailable;

    public Message translateTo(Message message, long sequence)
    {
        message.type.set(EventType.SECTION_UPDATED);
        
        SectionUpdated sectionUpdated = message.event.asSectionUpdated;
        
        sectionUpdated.concertId.set(concertId);
        sectionUpdated.sectionId.set(sectionId);
        sectionUpdated.version.set(sequence);
        sectionUpdated.seatsAvailable.set(seatsAvailable);
        
        return message; 
    }

    public void set(long concertId, long sectionId, int seatsAvailable)
    {
        this.concertId = concertId;
        this.sectionId = sectionId;
        this.seatsAvailable = seatsAvailable;
    }
}
