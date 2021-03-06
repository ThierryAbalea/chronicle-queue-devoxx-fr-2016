package com.github.thierryabalea.ticket_sales.method_call;

import com.github.thierryabalea.ticket_sales.ConcertFactory;
import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;

public class SeedClient {

    public static void createConcerts(CommandHandler commandHandler) {
        ConcertFactory.createConcerts().stream().forEachOrdered(commandHandler::onCreateConcert);
    }
}
