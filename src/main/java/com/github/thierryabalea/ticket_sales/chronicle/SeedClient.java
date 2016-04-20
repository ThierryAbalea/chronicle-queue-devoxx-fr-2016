package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import static com.github.thierryabalea.ticket_sales.ConcertFactory.createConcerts;
import static java.lang.String.format;

public class SeedClient {

    public static void main(String[] args) throws Exception {
        String createConcertQueuePath = format("%s/%s", OS.TARGET, "createConcertQueue");
        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(createConcertQueuePath).build()) {
            CommandHandler commandHandler = queue.createAppender()
                    .methodWriterBuilder(CommandHandler.class)
                    .get();
            createConcerts().stream().forEachOrdered(commandHandler::onCreateConcert);
        }
    }
}
