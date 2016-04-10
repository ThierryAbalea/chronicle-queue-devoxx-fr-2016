package com.github.thierryabalea.ticket_sales.framework;

import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import com.github.thierryabalea.ticket_sales.domain.ConcertServiceListener;
import com.github.thierryabalea.ticket_sales.translate.PurchaseRejectedTranslator;
import com.lmax.disruptor.EventPublisher;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.api.RejectionReason;
import com.github.thierryabalea.ticket_sales.domain.Concert;
import com.github.thierryabalea.ticket_sales.translate.ConcertAvailableTranslator;
import com.github.thierryabalea.ticket_sales.translate.PurchaseApprovedTranslator;
import com.github.thierryabalea.ticket_sales.translate.SectionUpdatedTranslator;

public class Publisher implements ConcertServiceListener
{
    private final EventPublisher<Message> eventPublisher;
    private final PurchaseApprovedTranslator purchaseApprovedTranslator = new PurchaseApprovedTranslator();
    private final ConcertAvailableTranslator concertAvailableTranslator = new ConcertAvailableTranslator();
    private final PurchaseRejectedTranslator purchaseRejectedTranslator = new PurchaseRejectedTranslator();
    private final SectionUpdatedTranslator   sectionUpdatedTranslator   = new SectionUpdatedTranslator();
    
    public Publisher(EventPublisher<Message> eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }
    
    public void onConcertAvailable(Concert concert)
    {
        concertAvailableTranslator.set(concert);
        eventPublisher.publishEvent(concertAvailableTranslator);
    }
    
    public void onPurchaseApproved(TicketPurchase ticketPurchase)
    {
        purchaseApprovedTranslator.set(ticketPurchase);
        eventPublisher.publishEvent(purchaseApprovedTranslator);
    }
    
    public void onPurchaseRejected(RejectionReason rejectionReason, TicketPurchase ticketPurchase)
    {
        purchaseRejectedTranslator.set(rejectionReason, ticketPurchase);
        eventPublisher.publishEvent(purchaseRejectedTranslator);
    }
    
    public void onSectionUpdated(long concertId, long sectionId, int seatsAvailable)
    {
        sectionUpdatedTranslator.set(concertId, sectionId, seatsAvailable);
        eventPublisher.publishEvent(sectionUpdatedTranslator);
    }
}
