package com.github.thierryabalea.ticket_sales.api.command;

import com.github.thierryabalea.ticket_sales.api.SectionSeating;
import net.openhft.chronicle.wire.AbstractMarshallable;

import java.util.List;

public class CreateConcert extends AbstractMarshallable {
    public final long concertId;
    public final long version;
    public final String name;
    public final String venue;
    public final short numSections;
    public final List<SectionSeating> sections;

    public CreateConcert(long concertId, long version, String name, String venue, short numSections, List<SectionSeating> sections) {
        this.concertId = concertId;
        this.version = version;
        this.name = name;
        this.venue = venue;
        this.numSections = numSections;
        this.sections = sections;
    }
}