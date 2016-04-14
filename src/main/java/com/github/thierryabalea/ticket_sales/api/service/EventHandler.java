package com.github.thierryabalea.ticket_sales.api.service;

import com.github.thierryabalea.ticket_sales.api.*;

public interface EventHandler {
    void onConcertAvailable(ConcertCreated concertCreated);

    void onAllocationApproved(AllocationApproved allocationApproved);

    void onAllocationRejected(AllocationRejected allocationRejected);

    void onSectionUpdated(SectionUpdated sectionUpdated);
}
