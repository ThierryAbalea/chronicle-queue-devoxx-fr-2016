package com.github.thierryabalea.ticket_sales.api.event;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class AllocationApproved extends AbstractMarshallable {
    public final long accountId;
    public final long requestId;
    public final int numSeats;

    public AllocationApproved(long accountId, long requestId, int numSeats) {
        this.accountId = accountId;
        this.requestId = requestId;
        this.numSeats = numSeats;
    }
}
