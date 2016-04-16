package com.github.thierryabalea.ticket_sales.web.json;

import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.SectionSeating;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class ConcertCreatedToJson {

    public JSONObject toJson(ConcertCreated concertCreated) {
        JSONObject json = new JSONObject();

        json.put("concertId", concertCreated.concertId);
        json.put("version", concertCreated.version);
        json.put("name", concertCreated.name);
        json.put("venue", concertCreated.venue);
        json.put("type", "CONCERT_CREATED");

        JSONArray jsonSections = new JSONArray();
        json.put("sections", jsonSections);

        for (int i = 0, n = concertCreated.numSections; i < n; i++) {
            JSONObject sectionJson = new JSONObject();
            SectionSeating section = concertCreated.sections.get(i);

            sectionJson.put("sectionId", section.sectionId);
            sectionJson.put("name", section.name);
            sectionJson.put("price", section.price);
            sectionJson.put("seats", section.seats);

            jsonSections.add(sectionJson);
        }

        return json;
    }

}
