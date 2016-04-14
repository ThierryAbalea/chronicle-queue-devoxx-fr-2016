package com.github.thierryabalea.ticket_sales.domain;

import com.github.thierryabalea.ticket_sales.api.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;

public interface CommandHandler {
    void onConcertCreated(ConcertCreated eventCreated);

    void onTicketPurchase(TicketPurchase ticketPurchase);
}

