package com.github.thierryabalea.ticket_sales.api;

import net.openhft.chronicle.wire.AbstractMarshallable;


public class TicketPurchase extends AbstractMarshallable implements TicketingEvent{
    public final long concertId;
    public final long sectionId;
    public final int numSeats;
    public final long accountId;
    public final long requestId;

    public TicketPurchase(long concertId, long sectionId, int numSeats, long accountId, long requestId) {
        this.concertId = concertId;
        this.sectionId = sectionId;
        this.numSeats = numSeats;
        this.accountId = accountId;
        this.requestId = requestId;
    }

    public static EventType type() {
        return EventType.TICKET_PURCHASE;
    }
}
