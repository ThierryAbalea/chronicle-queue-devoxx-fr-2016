package com.github.thierryabalea.ticket_sales.api.event;

import net.openhft.chronicle.wire.AbstractMarshallable;

/**
 * The system create frequently instances of this class. We make them mutable in order to reduce the garbage.
 */
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
