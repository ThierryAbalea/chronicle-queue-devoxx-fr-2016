package com.github.thierryabalea.ticket_sales.web.json;

import net.minidev.json.JSONObject;

import com.github.thierryabalea.ticket_sales.api.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.EventType;

public class AllocationApprovedToJson
{
    public JSONObject toJson(AllocationApproved allocationApproved)
    {
        JSONObject json = new JSONObject();
        
        json.put("accountId", allocationApproved.accountId);
        json.put("requestId", allocationApproved.requestId);
        json.put("numSeats", allocationApproved.numSeats);
        json.put("type", EventType.ALLOCATION_APPROVED.name());
        
        return json;
    }
}
