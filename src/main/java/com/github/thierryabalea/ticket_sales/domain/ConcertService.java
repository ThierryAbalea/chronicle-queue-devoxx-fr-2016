package com.github.thierryabalea.ticket_sales.domain;

import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.RejectionReason;
import com.github.thierryabalea.ticket_sales.api.SectionSeating;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.HashMap;

public class ConcertService implements Concert.Observer
{
    private final ConcertServiceListener listener;
    private final Long2ObjectMap<Concert> concertRepository = new Long2ObjectOpenHashMap<Concert>();

    public ConcertService(ConcertServiceListener listener)
    {
        this.listener = listener;
    }

    public void on(TicketPurchase ticketPurchase)
    {
        Concert concert = concertRepository.get(ticketPurchase.concertId);
        if (concert == null)
        {
            listener.onPurchaseRejected(RejectionReason.CONCERT_DOES_NOT_EXIST, ticketPurchase);
            return;
        }
        
        Section section = concert.getSection(ticketPurchase.sectionId);
        if (section == null)
        {
            listener.onPurchaseRejected(RejectionReason.SECTION_DOES_NOT_EXIST, ticketPurchase);
            return;
        }
        
        int numSeats = ticketPurchase.numSeats;
        if (concert.getSeating(section).getAvailableSeats() < numSeats)
        {
            listener.onPurchaseRejected(RejectionReason.NOT_ENOUGH_SEATS, ticketPurchase);
            return;
        }
        
        concert.allocateSeating(section, numSeats);
        listener.onPurchaseApproved(ticketPurchase);
    }
    
    public void on(ConcertCreated eventCreated)
    {
        HashMap<Section, Seating> seatingBySection = Maps.newHashMap();
        
        for (int i = 0, n = eventCreated.numSections; i < n; i++)
        {
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
        listener.onConcertAvailable(concert);
    }
    
    @Override
    public void onSeatsAllocated(Concert event, Section section, Seating seating)
    {
        listener.onSectionUpdated(event.getId(), section.getId(), seating.getAvailableSeats());
    }
}
