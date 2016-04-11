package com.github.thierryabalea.ticket_sales.domain;

import com.github.thierryabalea.ticket_sales.api.*;

public interface ConcertServiceListener {
    void onConcertAvailable(ConcertCreated concertCreated);

    void onAllocationApproved(AllocationApproved allocationApproved);

    void onAllocationRejected(AllocationRejected allocationRejected);

    void onSectionUpdated(SectionUpdated sectionUpdated);
}
