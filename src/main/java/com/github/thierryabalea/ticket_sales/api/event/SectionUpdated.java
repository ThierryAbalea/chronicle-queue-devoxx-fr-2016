package com.github.thierryabalea.ticket_sales.api.event;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class SectionUpdated extends AbstractMarshallable {
    public long concertId;
    public long sectionId;
    public long version;
    public int seatsAvailable;

    public SectionUpdated init(long concertId, long sectionId, long version, int seatsAvailable) {
        this.concertId = concertId;
        this.sectionId = sectionId;
        this.version = version;
        this.seatsAvailable = seatsAvailable;
        return this;
    }
}
