package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import static com.github.thierryabalea.ticket_sales.ConcertFactory.createConcerts;
import static java.lang.String.format;

public class SeedClient {

    public static void main(String[] args) throws Exception {
        String concertServiceQueue = format("%s/%s", OS.TARGET, "concertServiceQueue");

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(concertServiceQueue).build()) {
            ConcertService concertService = queue.createAppender().methodWriter(ConcertService.class);
            createConcerts().stream().forEachOrdered(concertService::onConcertCreated);
        }
    }
}
