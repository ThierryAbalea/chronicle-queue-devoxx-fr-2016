package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.api.event.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.event.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.event.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.event.SectionUpdated;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
import net.openhft.chronicle.core.util.NanoSampler;

public class EventHandlerSampler implements EventHandler {

    private final EventHandler eventHandler;
    private final NanoSampler endToEnd;
    long ts0;


    public EventHandlerSampler(EventHandler eventHandler,
                               NanoSampler endToEnd) {
        this.eventHandler = eventHandler;
        this.endToEnd = endToEnd;
    }

    @Override
    public void onConcertAvailable(ConcertCreated concertCreated) {
        eventHandler.onConcertAvailable(concertCreated);
    }

    @Override
    public void onAllocationApproved(AllocationApproved allocationApproved) {
        eventHandler.onAllocationApproved(allocationApproved);
        long time = System.nanoTime() - ts0;
        endToEnd.sampleNanos(time);
    }

    @Override
    public void onAllocationRejected(AllocationRejected allocationRejected) {
        eventHandler.onAllocationRejected(allocationRejected);
    }

    @Override
    public void onSectionUpdated(SectionUpdated sectionUpdated) {
        eventHandler.onSectionUpdated(sectionUpdated);
    }
}
