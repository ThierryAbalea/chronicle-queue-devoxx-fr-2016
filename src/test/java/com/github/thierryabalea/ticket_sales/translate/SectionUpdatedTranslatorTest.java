package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.SectionUpdated;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SectionUpdatedTranslatorTest {

    @Test
    public void shouldTranslate() {
        Message message = new Message();
        SectionUpdatedTranslator.translateTo(message, 0, 1234, 5678, 90);

        assertThat(message.type.get(), is((Enum) EventType.SECTION_UPDATED));
        SectionUpdated sectionUpdated = message.event.asSectionUpdated;

        assertThat(sectionUpdated.concertId.get(), is(1234L));
        assertThat(sectionUpdated.sectionId.get(), is(5678L));
        assertThat(sectionUpdated.seatsAvailable.get(), is(90));
    }

}
