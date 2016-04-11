package com.github.thierryabalea.ticket_sales.api;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class AllocationRejected extends AbstractMarshallable implements TicketingEvent{
    public final long accountId;
    public final long requestId;
    public final RejectionReason reason;

    public AllocationRejected(long accountId, long requestId, RejectionReason reason) {
        this.accountId = accountId;
        this.requestId = requestId;
        this.reason = reason;
    }
}