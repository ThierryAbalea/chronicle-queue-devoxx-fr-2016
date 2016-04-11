package com.github.thierryabalea.ticket_sales.api;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class Poll extends AbstractMarshallable implements TicketingEvent {
    public final long accountId;
    public final long version;

    public Poll(long accountId, long version) {
        this.accountId = accountId;
        this.version = version;
    }
}
