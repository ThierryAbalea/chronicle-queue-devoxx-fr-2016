package com.github.thierryabalea.ticket_sales.translate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import com.github.thierryabalea.ticket_sales.api.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.RejectionReason;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import org.junit.Test;

import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;

public class PurchaseRejectedTranslatorTest
{

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldTranslate()
    {
        PurchaseRejectedTranslator translator = new PurchaseRejectedTranslator();
        TicketPurchase ticketPurchase = new TicketPurchase();
        ticketPurchase.accountId.set(11L);
        ticketPurchase.requestId.set(13L);
        ticketPurchase.numSeats.set(4);
        ticketPurchase.concertId.set(17L);
        ticketPurchase.sectionId.set(21L);
        
        translator.set(RejectionReason.NOT_ENOUGH_SEATS, ticketPurchase);

        Message output = translator.translateTo(new Message(), 0);
        
        assertThat(output.type.get(), is((Enum) EventType.ALLOCATION_REJECTED));
        
        AllocationRejected allocationRejected = output.event.asAllocationRejected;
        assertThat(allocationRejected.accountId.get(), is(ticketPurchase.accountId.get()));
        assertThat(allocationRejected.requestId.get(), is(ticketPurchase.requestId.get()));
        assertThat(allocationRejected.reason.get(), is((Enum) RejectionReason.NOT_ENOUGH_SEATS));
    }
}
