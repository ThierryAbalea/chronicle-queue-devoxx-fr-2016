package com.github.thierryabalea.ticket_sales.web.json;

import com.github.thierryabalea.ticket_sales.api.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.RejectionReason;
import net.minidev.json.JSONObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AllocationRejectedToJsonTest
{

    @Test
    public void shouldTranslateToJson()
    {
        AllocationRejected allocationRejected = new AllocationRejected(
                12345L,
                765873645L,
                RejectionReason.NOT_ENOUGH_SEATS
        );
        
        AllocationRejectedToJson translator = new AllocationRejectedToJson();
        
        JSONObject json = translator.toJson(allocationRejected);
        
        assertThat(json.get("accountId"), is((Object) allocationRejected.accountId));
        assertThat(json.get("requestId"), is((Object) allocationRejected.requestId));
        assertThat(json.get("reason"),    is((Object) allocationRejected.reason));
        assertThat(json.get("type"),      is((Object) EventType.ALLOCATION_REJECTED.name()));
    }

}
