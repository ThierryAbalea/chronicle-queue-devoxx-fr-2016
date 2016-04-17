package com.github.thierryabalea.ticket_sales.api.command;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class TicketPurchase extends AbstractMarshallable {
    public final long sectionId;
    public final int numSeats;
    public final long accountId;
    public final long requestId;
    public long concertId; // make TicketPurchase mutable to reduce garbage

    public TicketPurchase(long concertId, long sectionId, int numSeats, long accountId, long requestId) {
        this.concertId = concertId;
        this.sectionId = sectionId;
        this.numSeats = numSeats;
        this.accountId = accountId;
        this.requestId = requestId;
    }
}
