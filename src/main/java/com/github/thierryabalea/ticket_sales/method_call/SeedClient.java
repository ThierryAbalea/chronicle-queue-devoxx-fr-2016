package com.github.thierryabalea.ticket_sales.method_call;

import com.github.thierryabalea.ticket_sales.ConcertFactory;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;

public class SeedClient {

    public static void createConcerts(ConcertService concertService) throws Exception {
        ConcertFactory.createConcerts().stream().forEachOrdered(concertService::onConcertCreated);
    }
}
