package com.github.thierryabalea.ticket_sales.web.json;

import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.SectionSeating;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConcertCreatedAsJsonTest
{

    @Test
    public void shouldConvetToJson()
    {
        ConcertCreated concertCreated = new ConcertCreated(
                12345L,
                345L,
                "Red Hot Chili Peppers",
                "Albert Hall",
                (short) 2,
                Arrays.asList(
                        new SectionSeating(5,"East", 75.50F, 100),
                        new SectionSeating(6,"West", 76.50F, 200)
                )
        );
        
        ConcertCreatedToJson tranlator = new ConcertCreatedToJson();
        JSONObject json = tranlator.toJson(concertCreated);
        
        assertThat(json.get("concertId"), is((Object) concertCreated.concertId));
        assertThat(json.get("version"),   is((Object) concertCreated.version));
        assertThat(json.get("name"),      is((Object) concertCreated.name));
        assertThat(json.get("venue"),     is((Object) concertCreated.venue));
        assertThat(json.get("type"),      is((Object) EventType.CONCERT_CREATED.name()));
        
        assertThat(json.get("sections"),  instanceOf(JSONArray.class));
        JSONArray sections = (JSONArray) json.get("sections");
        assertThat(sections.size(), is((int) concertCreated.numSections));
        
        JSONObject section1 = (JSONObject) sections.get(0);        
        assertThat(section1.get("sectionId"), is((Object) concertCreated.sections.get(0).sectionId));
        assertThat(section1.get("name"),      is((Object) concertCreated.sections.get(0).name));
        assertThat(section1.get("price"),     is((Object) concertCreated.sections.get(0).price));
        assertThat(section1.get("seats"),     is((Object) concertCreated.sections.get(0).seats));
        
        JSONObject section2 = (JSONObject) sections.get(1);        
        assertThat(section2.get("sectionId"), is((Object) concertCreated.sections.get(1).sectionId));
        assertThat(section2.get("name"),      is((Object) concertCreated.sections.get(1).name));
        assertThat(section2.get("price"),     is((Object) concertCreated.sections.get(1).price));
        assertThat(section2.get("seats"),     is((Object) concertCreated.sections.get(1).seats));
    }

}
