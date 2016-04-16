package com.github.thierryabalea.ticket_sales.api.event;

import net.openhft.chronicle.wire.AbstractMarshallable;

public class SectionSeating extends AbstractMarshallable {
    public final long sectionId;
    public final String name;
    public final float price;
    public final int seats;

    public SectionSeating(long sectionId, String name, float price, int seats) {
        this.sectionId = sectionId;
        this.name = name;
        this.price = price;
        this.seats = seats;
    }
}
