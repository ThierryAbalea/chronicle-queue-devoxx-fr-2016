package com.github.thierryabalea.ticket_sales.api;

import net.openhft.chronicle.wire.AbstractMarshallable;

import java.util.List;

public class ConcertCreated extends AbstractMarshallable implements TicketingEvent {
    public final long concertId;
    public final long version;
    public final String name;
    public final String venue;
    public final short numSections;
    public final List<SectionSeating> sections;

    public ConcertCreated(long concertId, long version, String name, String venue, short numSections, List<SectionSeating> sections) {
        this.concertId = concertId;
        this.version = version;
        this.name = name;
        this.venue = venue;
        this.numSections = numSections;
        this.sections = sections;
    }
}