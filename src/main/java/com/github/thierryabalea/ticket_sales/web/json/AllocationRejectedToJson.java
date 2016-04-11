package com.github.thierryabalea.ticket_sales.web.json;

import net.minidev.json.JSONObject;

import com.github.thierryabalea.ticket_sales.api.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.EventType;

public class AllocationRejectedToJson
{
    public JSONObject toJson(AllocationRejected allocationRejected)
    {
        JSONObject json = new JSONObject();
        
        json.put("accountId", allocationRejected.accountId);
        json.put("requestId", allocationRejected.requestId);
        json.put("reason",    allocationRejected.reason);
        json.put("type",      EventType.ALLOCATION_REJECTED.name());
        
        return json;
    }
}
