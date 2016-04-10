package com.github.thierryabalea.ticket_sales.web.json;

import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.SectionSeating;

public class ConcertCreatedToJson
{

    public JSONObject toJson(ConcertCreated concertCreated)
    {
        JSONObject json = new JSONObject();
        
        json.put("concertId", concertCreated.concertId.get());
        json.put("version",   concertCreated.version.get());
        json.put("name",      concertCreated.name.get());
        json.put("venue",     concertCreated.venue.get());
        json.put("type",      EventType.CONCERT_CREATED.name());
        
        JSONArray jsonSections = new JSONArray();
        json.put("sections", jsonSections);
        
        for (int i = 0, n = concertCreated.numSections.get(); i < n; i++)
        {
            JSONObject sectionJson = new JSONObject();
            SectionSeating section = concertCreated.sections[i];
            
            sectionJson.put("sectionId", section.sectionId.get());
            sectionJson.put("name",      section.name.get());
            sectionJson.put("price",     section.price.get());
            sectionJson.put("seats",     section.seats.get());
            
            jsonSections.add(sectionJson);
        }
        
        return json;
    }

}
