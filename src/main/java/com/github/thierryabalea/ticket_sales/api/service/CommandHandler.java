package com.github.thierryabalea.ticket_sales.api.service;

import com.github.thierryabalea.ticket_sales.api.command.CreateConcert;
import com.github.thierryabalea.ticket_sales.api.command.TicketPurchase;

public interface CommandHandler {
    void onCreateConcert(CreateConcert createConcert);

    void onTicketPurchase(TicketPurchase ticketPurchase);
}

