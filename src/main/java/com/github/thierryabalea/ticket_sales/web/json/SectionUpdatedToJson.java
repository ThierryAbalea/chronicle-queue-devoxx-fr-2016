package com.github.thierryabalea.ticket_sales.web.json;

import com.github.thierryabalea.ticket_sales.api.SectionUpdated;
import net.minidev.json.JSONObject;

public class SectionUpdatedToJson {
    public JSONObject toJson(SectionUpdated sectionUpdated) {
        JSONObject json = new JSONObject();

        json.put("concertId", sectionUpdated.concertId);
        json.put("sectionId", sectionUpdated.sectionId);
        json.put("version", sectionUpdated.version);
        json.put("seatsAvailable", sectionUpdated.seatsAvailable);
        json.put("type", "SECTION_UPDATED");

        return json;
    }
}
