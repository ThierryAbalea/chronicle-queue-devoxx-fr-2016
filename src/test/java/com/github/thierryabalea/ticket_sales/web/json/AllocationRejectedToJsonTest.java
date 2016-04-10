package com.github.thierryabalea.ticket_sales.web.json;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.github.thierryabalea.ticket_sales.api.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.RejectionReason;
import net.minidev.json.JSONObject;

import org.junit.Test;

import com.github.thierryabalea.ticket_sales.api.EventType;

public class AllocationRejectedToJsonTest
{

    @Test
    public void shouldTranslateToJson()
    {
        AllocationRejected allocationRejected = new AllocationRejected();
        allocationRejected.accountId.set(12345L);
        allocationRejected.requestId.set(765873645L);
        allocationRejected.reason.set(RejectionReason.NOT_ENOUGH_SEATS);
        
        AllocationRejectedToJson translator = new AllocationRejectedToJson();
        
        JSONObject json = translator.toJson(allocationRejected);
        
        assertThat(json.get("accountId"), is((Object) allocationRejected.accountId.get()));
        assertThat(json.get("requestId"), is((Object) allocationRejected.requestId.get()));
        assertThat(json.get("reason"),    is((Object) allocationRejected.reason.get()));
        assertThat(json.get("type"),      is((Object) EventType.ALLOCATION_REJECTED.name()));
    }

}
