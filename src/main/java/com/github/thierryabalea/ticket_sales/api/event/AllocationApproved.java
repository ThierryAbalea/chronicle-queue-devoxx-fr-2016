package com.github.thierryabalea.ticket_sales.api.event;

import net.openhft.chronicle.wire.AbstractMarshallable;

/**
 * The system create frequently instances of this class. We make them mutable in order to reduce the garbage.
 */
public class AllocationApproved extends AbstractMarshallable {
    public long accountId;
    public long requestId;
    public int numSeats;

    public AllocationApproved init(long accountId, long requestId, int numSeats) {
        this.accountId = accountId;
        this.requestId = requestId;
        this.numSeats = numSeats;
        return this;
    }
}
