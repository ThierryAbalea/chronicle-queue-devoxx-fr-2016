package com.github.thierryabalea.ticket_sales.web.json;

import com.github.thierryabalea.ticket_sales.api.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.EventType;
import net.minidev.json.JSONObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AllocationApprovedToJsonTest
{

    @Test
    public void shouldConvertToJson()
    {
        AllocationApprovedToJson approvedToJson = new AllocationApprovedToJson();
        
        AllocationApproved allocationApproved = new AllocationApproved(
                12345,
                67234234L,
                2
        );
        
        JSONObject json = approvedToJson.toJson(allocationApproved);
        
        assertThat(json.get("accountId"), is((Object) allocationApproved.accountId));
        assertThat(json.get("numSeats"), is((Object) allocationApproved.numSeats));
        assertThat(json.get("requestId"), is((Object) allocationApproved.requestId));
        assertThat(json.get("type"), is((Object) EventType.ALLOCATION_APPROVED.name()));
    }
}
