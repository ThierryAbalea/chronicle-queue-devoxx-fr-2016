package com.github.thierryabalea.ticket_sales.api.service;

import com.github.thierryabalea.ticket_sales.api.event.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.event.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.event.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.event.SectionUpdated;

public interface EventHandler {
    void onConcertAvailable(ConcertCreated concertCreated);

    void onAllocationApproved(AllocationApproved allocationApproved);

    void onAllocationRejected(AllocationRejected allocationRejected);

    void onSectionUpdated(SectionUpdated sectionUpdated);
}
