package com.github.thierryabalea.ticket_sales.translate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.SectionUpdated;

public class SectionUpdatedTranslatorTest
{

    @Test
    public void shouldTranslate()
    {
        SectionUpdatedTranslator sectionUpdatedTranslator = new SectionUpdatedTranslator();
        
        sectionUpdatedTranslator.set(1234, 5678, 90);
        
        Message output = sectionUpdatedTranslator.translateTo(new Message(), 0);
        
        assertThat(output.type.get(), is((Enum) EventType.SECTION_UPDATED));
        SectionUpdated sectionUpdated = output.event.asSectionUpdated;
        
        assertThat(sectionUpdated.concertId.get(), is(1234L));
        assertThat(sectionUpdated.sectionId.get(), is(5678L));
        assertThat(sectionUpdated.seatsAvailable.get(), is(90));
    }

}
