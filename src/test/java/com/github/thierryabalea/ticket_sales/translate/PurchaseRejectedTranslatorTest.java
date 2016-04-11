package com.github.thierryabalea.ticket_sales.translate;

import com.github.thierryabalea.ticket_sales.api.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.EventType;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.RejectionReason;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PurchaseRejectedTranslatorTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldTranslate() {
        TicketPurchase ticketPurchase = new TicketPurchase(
                17L,
                21L,
                4,
                11L,
                13L
        );

        Message message = new Message();
        PurchaseRejectedTranslator.translateTo(message, 0, RejectionReason.NOT_ENOUGH_SEATS, ticketPurchase);

        assertThat(message.type, is((Enum) EventType.ALLOCATION_REJECTED));

        AllocationRejected allocationRejected = (AllocationRejected) message.event;
        assertThat(allocationRejected.accountId, is(ticketPurchase.accountId));
        assertThat(allocationRejected.requestId, is(ticketPurchase.requestId));
        assertThat(allocationRejected.reason, is((Enum) RejectionReason.NOT_ENOUGH_SEATS));
    }
}
