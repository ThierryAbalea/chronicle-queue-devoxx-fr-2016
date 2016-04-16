package com.github.thierryabalea.ticket_sales.web.json;

import com.github.thierryabalea.ticket_sales.api.event.AllocationApproved;
import net.minidev.json.JSONObject;

public class AllocationApprovedToJson {
    public JSONObject toJson(AllocationApproved allocationApproved) {
        JSONObject json = new JSONObject();

        json.put("accountId", allocationApproved.accountId);
        json.put("requestId", allocationApproved.requestId);
        json.put("numSeats", allocationApproved.numSeats);
        json.put("type", "ALLOCATION_APPROVED");

        return json;
    }
}
