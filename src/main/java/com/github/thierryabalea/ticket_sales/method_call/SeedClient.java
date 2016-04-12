package com.github.thierryabalea.ticket_sales.method_call;

import com.github.thierryabalea.ticket_sales.CreateConcertsFactory;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;

public class SeedClient {

    public static void createConcerts(ConcertService concertService) throws Exception {
        CreateConcertsFactory.createConcerts().stream().forEachOrdered(concertService::onConcertCreated);
    }
}
