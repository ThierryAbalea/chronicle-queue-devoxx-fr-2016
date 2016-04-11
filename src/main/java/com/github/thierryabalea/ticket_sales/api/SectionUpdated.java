package com.github.thierryabalea.ticket_sales.api;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class SectionUpdated extends AbstractMarshallable implements TicketingEvent {
    public final long concertId;
    public final long sectionId;
    public final long version;
    public final int seatsAvailable;

    public SectionUpdated(long concertId, long sectionId, long version, int seatsAvailable) {
        this.concertId = concertId;
        this.sectionId = sectionId;
        this.version = version;
        this.seatsAvailable = seatsAvailable;
    }
}
