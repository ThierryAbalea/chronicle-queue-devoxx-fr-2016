package com.github.thierryabalea.ticket_sales.api.event;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class SectionUpdated extends AbstractMarshallable {
    public long concertId;
    public long sectionId;
    public long version;
    public int seatsAvailable;

    private static final SectionUpdated sectionUpdated = new SectionUpdated(0, 0, 0, 0);

    public static SectionUpdated newSectionUpdated(long concertId, long sectionId, long version, int seatsAvailable) {
        sectionUpdated.concertId = concertId;
        sectionUpdated.sectionId = sectionId;
        sectionUpdated.version = version;
        sectionUpdated.seatsAvailable = seatsAvailable;
        return sectionUpdated;
    }

    public SectionUpdated(long concertId, long sectionId, long version, int seatsAvailable) {
        this.concertId = concertId;
        this.sectionId = sectionId;
        this.version = version;
        this.seatsAvailable = seatsAvailable;
    }
}
