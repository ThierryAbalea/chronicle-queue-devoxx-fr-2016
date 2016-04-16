package com.github.thierryabalea.ticket_sales.api.event;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class AllocationRejected extends AbstractMarshallable {

    public enum RejectionReason {
        CONCERT_DOES_NOT_EXIST,
        SECTION_DOES_NOT_EXIST,
        NOT_ENOUGH_SEATS
    }

    public final long accountId;
    public final long requestId;
    public final RejectionReason reason;

    public AllocationRejected(long accountId, long requestId, RejectionReason reason) {
        this.accountId = accountId;
        this.requestId = requestId;
        this.reason = reason;
    }
}