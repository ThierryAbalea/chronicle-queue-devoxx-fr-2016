package com.github.thierryabalea.ticket_sales.domain;

import com.github.thierryabalea.ticket_sales.api.*;
import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static com.github.thierryabalea.ticket_sales.api.RejectionReason.CONCERT_DOES_NOT_EXIST;
import static com.github.thierryabalea.ticket_sales.api.RejectionReason.NOT_ENOUGH_SEATS;
import static com.github.thierryabalea.ticket_sales.api.RejectionReason.SECTION_DOES_NOT_EXIST;

public class ConcertService implements Concert.Observer, CommandHandler {
    private final EventHandler eventHandler;
    private final Long2ObjectMap<Concert> concertRepository = new Long2ObjectOpenHashMap<Concert>();
    private long sectionVersion = 0;

    public ConcertService(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void onConcertCreated(ConcertCreated eventCreated) {
        HashMap<Section, Seating> seatingBySection = Maps.newHashMap();

        for (int i = 0, n = eventCreated.numSections; i < n; i++) {
            SectionSeating sectionSeating = eventCreated.sections.get(i);
            Section section = new Section(sectionSeating.sectionId,
                    sectionSeating.name,
                    sectionSeating.price);
            Seating seating = new Seating(sectionSeating.seats);

            seatingBySection.put(section, seating);
        }

        Concert concert = new Concert(eventCreated.concertId,
                eventCreated.name,
                eventCreated.venue,
                seatingBySection);
        concertRepository.put(concert.getId(), concert);
        concert.addObserver(this);
        eventHandler.onConcertAvailable(eventCreated);
    }

    @Override
    public void onTicketPurchase(TicketPurchase ticketPurchase) {
        Concert concert = concertRepository.get(ticketPurchase.concertId);
        if (concert == null) {
            reject(ticketPurchase, CONCERT_DOES_NOT_EXIST);
            return;
        }

        Section section = concert.getSection(ticketPurchase.sectionId);
        if (section == null) {
            reject(ticketPurchase, SECTION_DOES_NOT_EXIST);
            return;
        }

        int numSeats = ticketPurchase.numSeats;
        if (concert.getSeating(section).getAvailableSeats() < numSeats) {
            reject(ticketPurchase, NOT_ENOUGH_SEATS);
            return;
        }

        concert.allocateSeating(section, numSeats);

        AllocationApproved allocationApproved = buildAllocationApproved(ticketPurchase);
        eventHandler.onAllocationApproved(allocationApproved);
    }

    private void reject(TicketPurchase ticketPurchase, RejectionReason reason) {
        AllocationRejected rejected = buildAllocationRejected(ticketPurchase, reason);
        eventHandler.onAllocationRejected(rejected);
    }

    @NotNull
    private AllocationApproved buildAllocationApproved(TicketPurchase ticketPurchase) {
        return new AllocationApproved(
                ticketPurchase.accountId,
                ticketPurchase.requestId,
                ticketPurchase.numSeats
        );
    }

    @NotNull
    private AllocationRejected buildAllocationRejected(TicketPurchase ticketPurchase, RejectionReason reason) {
        return new AllocationRejected(
                ticketPurchase.accountId,
                ticketPurchase.requestId,
                reason
        );
    }

    @Override
    public void onSeatsAllocated(Concert event, Section section, Seating seating) {
        SectionUpdated sectionUpdated = new SectionUpdated(
                event.getId(),
                section.getId(),
                sectionVersion,
                seating.getAvailableSeats()
        );
        sectionVersion++;
        eventHandler.onSectionUpdated(sectionUpdated);
    }
}
