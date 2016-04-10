package com.github.thierryabalea.ticket_sales.domain;

import com.github.thierryabalea.ticket_sales.api.RejectionReason;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;

public interface ConcertServiceListener
{
    void onConcertAvailable(Concert concert);
    void onPurchaseApproved(TicketPurchase ticketPurchase);
    void onPurchaseRejected(RejectionReason rejectionReason, TicketPurchase ticketPurchase);
    void onSectionUpdated(long concertId, long sectionId, int seatsAvailable);
}
