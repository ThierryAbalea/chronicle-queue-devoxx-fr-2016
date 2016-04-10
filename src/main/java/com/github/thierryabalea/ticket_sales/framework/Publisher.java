package com.github.thierryabalea.ticket_sales.framework;

import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.RejectionReason;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import com.github.thierryabalea.ticket_sales.domain.Concert;
import com.github.thierryabalea.ticket_sales.domain.ConcertServiceListener;
import com.github.thierryabalea.ticket_sales.translate.ConcertAvailableTranslator;
import com.github.thierryabalea.ticket_sales.translate.PurchaseApprovedTranslator;
import com.github.thierryabalea.ticket_sales.translate.PurchaseRejectedTranslator;
import com.github.thierryabalea.ticket_sales.translate.SectionUpdatedTranslator;
import com.lmax.disruptor.RingBuffer;

public class Publisher implements ConcertServiceListener {
    private final RingBuffer<Message> ringBuffer;

    public Publisher(RingBuffer<Message> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onConcertAvailable(Concert concert) {
        ringBuffer.publishEvent(ConcertAvailableTranslator::translateTo, concert);
    }

    public void onPurchaseApproved(TicketPurchase ticketPurchase) {
        ringBuffer.publishEvent(PurchaseApprovedTranslator::translateTo, ticketPurchase);
    }

    public void onPurchaseRejected(RejectionReason rejectionReason, TicketPurchase ticketPurchase) {
        ringBuffer.publishEvent(PurchaseRejectedTranslator::translateTo, rejectionReason, ticketPurchase);
    }

    public void onSectionUpdated(long concertId, long sectionId, int seatsAvailable) {
        ringBuffer.publishEvent(SectionUpdatedTranslator::translateTo, concertId, sectionId, seatsAvailable);
    }
}
