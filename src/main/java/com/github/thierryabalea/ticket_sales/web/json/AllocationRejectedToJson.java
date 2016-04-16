package com.github.thierryabalea.ticket_sales.web.json;

import com.github.thierryabalea.ticket_sales.api.AllocationRejected;
import net.minidev.json.JSONObject;

public class AllocationRejectedToJson {
    public JSONObject toJson(AllocationRejected allocationRejected) {
        JSONObject json = new JSONObject();

        json.put("accountId", allocationRejected.accountId);
        json.put("requestId", allocationRejected.requestId);
        json.put("reason", allocationRejected.reason);
        json.put("type", "ALLOCATION_REJECTED");

        return json;
    }
}
