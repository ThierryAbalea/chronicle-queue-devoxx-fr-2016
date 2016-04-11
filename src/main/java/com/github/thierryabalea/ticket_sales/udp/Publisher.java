package com.github.thierryabalea.ticket_sales.udp;

import com.github.thierryabalea.ticket_sales.api.*;
import com.github.thierryabalea.ticket_sales.domain.ConcertServiceListener;
import com.github.thierryabalea.ticket_sales.udp.translate.ConcertCreatedTranslator;
import com.github.thierryabalea.ticket_sales.udp.translate.PurchaseApprovedTranslator;
import com.github.thierryabalea.ticket_sales.udp.translate.AllocationRejectedTranslator;
import com.github.thierryabalea.ticket_sales.udp.translate.SectionUpdatedTranslator;
import com.lmax.disruptor.RingBuffer;

public class Publisher implements ConcertServiceListener {
    private final RingBuffer<Message> ringBuffer;

    public Publisher(RingBuffer<Message> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onConcertAvailable(ConcertCreated concertCreated) {
        ringBuffer.publishEvent(ConcertCreatedTranslator::translateTo, concertCreated);
    }

    public void onAllocationApproved(AllocationApproved allocationApproved) {
        ringBuffer.publishEvent(PurchaseApprovedTranslator::translateTo, allocationApproved);
    }

    public void onAllocationRejected(AllocationRejected allocationRejected) {
        ringBuffer.publishEvent(AllocationRejectedTranslator::translateTo, allocationRejected);
    }

    public void onSectionUpdated(SectionUpdated sectionUpdated) {
        ringBuffer.publishEvent(SectionUpdatedTranslator::translateTo, sectionUpdated);
    }
}
