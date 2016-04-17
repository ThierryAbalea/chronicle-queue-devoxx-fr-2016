package com.github.thierryabalea.ticket_sales.api.event;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class AllocationApproved extends AbstractMarshallable {
    public long accountId;
    public long requestId;
    public int numSeats;

    private static final AllocationApproved allocationApproved = new AllocationApproved(0, 0, 0);

    public static AllocationApproved newAllocationApproved(long accountId, long requestId, int numSeats) {
        allocationApproved.accountId = accountId;
        allocationApproved.requestId = requestId;
        allocationApproved.numSeats = numSeats;
        return allocationApproved;
    }

    public AllocationApproved(long accountId, long requestId, int numSeats) {
        this.accountId = accountId;
        this.requestId = requestId;
        this.numSeats = numSeats;
    }
}
