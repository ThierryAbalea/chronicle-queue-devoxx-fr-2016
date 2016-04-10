package com.github.thierryabalea.ticket_sales.translate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import com.github.thierryabalea.ticket_sales.api.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import org.junit.Test;

import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;

public class PurchaseApprovedTranslatorTest
{
    @Test
    @SuppressWarnings("rawtypes")
    public void shouldTranslate()
    {
        PurchaseApprovedTranslator translator = new PurchaseApprovedTranslator();
        TicketPurchase ticketPurchase = new TicketPurchase();
        ticketPurchase.accountId.set(11L);
        ticketPurchase.requestId.set(13L);
        ticketPurchase.numSeats.set(4);
        ticketPurchase.concertId.set(17L);
        ticketPurchase.sectionId.set(21L);
        
        translator.set(ticketPurchase);
        
        Message output = translator.translateTo(new Message(), 0);
        
        assertThat(output.type.get(), is((Enum) EventType.ALLOCATION_APPROVED));
        AllocationApproved allocationApproved = output.event.asAllocationApproved;
        assertThat(allocationApproved.accountId.get(), is(ticketPurchase.accountId.get()));
        assertThat(allocationApproved.requestId.get(), is(ticketPurchase.requestId.get()));
        assertThat(allocationApproved.numSeats.get(), is(ticketPurchase.numSeats.get()));
    }
}
