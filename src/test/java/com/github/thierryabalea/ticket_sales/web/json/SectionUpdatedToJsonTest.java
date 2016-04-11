package com.github.thierryabalea.ticket_sales.web.json;

import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.SectionUpdated;
import net.minidev.json.JSONObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SectionUpdatedToJsonTest
{

    @Test
    public void shouldTranslate()
    {
        SectionUpdated sectionUpdate = new SectionUpdated(
                1231L,
                6543L,
                341231L,
                7897
        );
        
        SectionUpdatedToJson translator = new SectionUpdatedToJson();
        
        JSONObject json = translator.toJson(sectionUpdate);
        
        assertThat(json.get("concertId"),      is((Object) sectionUpdate.concertId));
        assertThat(json.get("sectionId"),      is((Object) sectionUpdate.sectionId));
        assertThat(json.get("version"),        is((Object) sectionUpdate.version));
        assertThat(json.get("seatsAvailable"), is((Object) sectionUpdate.seatsAvailable));
        assertThat(json.get("type"), is((Object) EventType.SECTION_UPDATED.name()));
    }

}
