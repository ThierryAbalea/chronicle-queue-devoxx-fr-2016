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
        TicketPurchase ticketPurchase = new TicketPurchase();
        ticketPurchase.accountId.set(11L);
        ticketPurchase.requestId.set(13L);
        ticketPurchase.numSeats.set(4);
        ticketPurchase.concertId.set(17L);
        ticketPurchase.sectionId.set(21L);

        Message message = new Message();
        PurchaseApprovedTranslator.translateTo(message, 0, ticketPurchase);

        assertThat(message.type.get(), is((Enum) EventType.ALLOCATION_APPROVED));
        AllocationApproved allocationApproved = message.event.asAllocationApproved;
        assertThat(allocationApproved.accountId.get(), is(ticketPurchase.accountId.get()));
        assertThat(allocationApproved.requestId.get(), is(ticketPurchase.requestId.get()));
        assertThat(allocationApproved.numSeats.get(), is(ticketPurchase.numSeats.get()));
    }
}
