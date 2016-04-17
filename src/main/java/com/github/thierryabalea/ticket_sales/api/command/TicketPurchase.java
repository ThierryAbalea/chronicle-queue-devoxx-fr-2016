package com.github.thierryabalea.ticket_sales.api.command;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class TicketPurchase extends AbstractMarshallable {
    public long concertId;
    public long sectionId;
    public int numSeats;
    public long accountId;
    public long requestId;

    private static final TicketPurchase ticketPurshase = new TicketPurchase(0, 0, 0, 0, 0);

    public static TicketPurchase newTicketPurchase(long concertId, long sectionId, int numSeats, long accountId, long requestId) {
        ticketPurshase.concertId = concertId;
        ticketPurshase.sectionId = sectionId;
        ticketPurshase.numSeats = numSeats;
        ticketPurshase.accountId = accountId;
        ticketPurshase.requestId = requestId;
        return ticketPurshase;
    }

    public TicketPurchase(long concertId, long sectionId, int numSeats, long accountId, long requestId) {
        this.concertId = concertId;
        this.sectionId = sectionId;
        this.numSeats = numSeats;
        this.accountId = accountId;
        this.requestId = requestId;
    }
}
