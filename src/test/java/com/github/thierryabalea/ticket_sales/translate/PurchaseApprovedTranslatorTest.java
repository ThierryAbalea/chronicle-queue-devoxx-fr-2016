package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PurchaseApprovedTranslatorTest {
    @Test
    @SuppressWarnings("rawtypes")
    public void shouldTranslate() {
        TicketPurchase ticketPurchase = new TicketPurchase(
                17L,
                21L,
                4,
                11L,
                13L
        );

        Message message = new Message();
        PurchaseApprovedTranslator.translateTo(message, 0, ticketPurchase);

        assertThat(message.type, is((Enum) EventType.ALLOCATION_APPROVED));
        AllocationApproved allocationApproved = (AllocationApproved) message.event;
        assertThat(allocationApproved.accountId, is(ticketPurchase.accountId));
        assertThat(allocationApproved.requestId, is(ticketPurchase.requestId));
        assertThat(allocationApproved.numSeats, is(ticketPurchase.numSeats));
    }
}
