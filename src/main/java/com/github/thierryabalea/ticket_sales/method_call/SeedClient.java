package com.github.thierryabalea.ticket_sales.method_call;

import com.github.thierryabalea.ticket_sales.ConcertFactory;
import com.github.thierryabalea.ticket_sales.domain.CommandHandler;

public class SeedClient {

    public static void createConcerts(CommandHandler commandHandler) throws Exception {
        ConcertFactory.createConcerts().stream().forEachOrdered(commandHandler::onConcertCreated);
    }
}
