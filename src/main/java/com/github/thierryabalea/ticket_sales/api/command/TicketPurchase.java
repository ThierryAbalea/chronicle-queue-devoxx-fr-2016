package com.github.thierryabalea.ticket_sales.api.command;

import net.openhft.chronicle.wire.AbstractMarshallable;

/**
 * The system create frequently instances of this class. We make them mutable in order to reduce the garbage.
 */
public class TicketPurchase extends AbstractMarshallable {
    public long accountId;
    public long requestId;
    public long concertId;
    public int numSeats;
    public long sectionId;

    public TicketPurchase() {
    }

    public TicketPurchase(long concertId, long sectionId, int numSeats, long accountId, long requestId) {
        this.concertId = concertId;
        this.sectionId = sectionId;
        this.numSeats = numSeats;
        this.accountId = accountId;
        this.requestId = requestId;
    }
}
