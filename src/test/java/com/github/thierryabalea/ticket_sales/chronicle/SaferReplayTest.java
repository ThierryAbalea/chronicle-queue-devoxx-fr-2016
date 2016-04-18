package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.api.command.CreateConcert;
import com.github.thierryabalea.ticket_sales.api.command.TicketPurchase;
import com.github.thierryabalea.ticket_sales.api.SectionSeating;
import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class SaferReplayTest {

    private static final long concertId = 1;
    private static final long sectionId = 1;
    private static final int numSeats = 1;
    private static final long accountId = 12;
    private static final long requestId = 76;

    @Test
    public void testBetterReplay() {
        String createConcertQueuePath = format("%s/%s", OS.TARGET, "createConcertQueuePath" + System.nanoTime());
        String commandHandlerQueuePath = format("%s/%s", OS.TARGET, "commandHandlerQueuePath-" + System.nanoTime());
        String eventHandlerQueuePath = format("%s/%s", OS.TARGET, "eventHandlerQueuePath-" + System.nanoTime());

        int numberOfEventsPerQueue = 4;
        // numberOfReadsPerQueue must be greater than numberOfEventsPerQueue to ensure than unexpected replay does not happens
        int numberOfReadsPerQueue = numberOfEventsPerQueue + 2;
        try {
            try (ChronicleQueue createConcertQueue = SingleChronicleQueueBuilder.binary(createConcertQueuePath).sourceId(1).build()) {
                CommandHandler commandHandler = createConcertQueue
                        .createAppender()
                        .methodWriterBuilder(CommandHandler.class)
                        .recordHistory(true)
                        .get();

                long currentConcertId = concertId;
                for (int i = 0; i < numberOfEventsPerQueue; i++) {
                    CreateConcert createConcert = new CreateConcert(
                            currentConcertId,
                            0,
                            "Red Hot Chili Peppers",
                            "Albert Hall",
                            (short) 1,
                            asList(new SectionSeating(sectionId, "Section A", 58.50F, Integer.MAX_VALUE))
                    );
                    commandHandler.onCreateConcert(createConcert);
                    currentConcertId++;
                }
            }

            try (ChronicleQueue commandHandlerQueue = SingleChronicleQueueBuilder.binary(commandHandlerQueuePath).sourceId(2).build()) {
                CommandHandler commandHandler = commandHandlerQueue
                        .createAppender()
                        .methodWriterBuilder(CommandHandler.class)
                        .recordHistory(true)
                        .get();
                TicketPurchase ticketPurchase = new TicketPurchase(concertId, sectionId, numSeats, accountId, requestId);
                for (int i = 0; i < numberOfEventsPerQueue; i++) {
                    commandHandler.onTicketPurchase(ticketPurchase);
                    ticketPurchase.concertId++;
                }
            }

            AtomicInteger onCreateConcertCallsCount = new AtomicInteger(0);
            AtomicInteger onTicketPurchaseCallsCount = new AtomicInteger(0);
            for (int i = 0; i < numberOfReadsPerQueue; i++) {

                try (ChronicleQueue eventHandlerQueue = SingleChronicleQueueBuilder.binary(eventHandlerQueuePath).sourceId(99).build();
                     ChronicleQueue createConcertQueue = SingleChronicleQueueBuilder.binary(createConcertQueuePath).sourceId(1).build();
                     ChronicleQueue commandHandlerQueue = SingleChronicleQueueBuilder.binary(commandHandlerQueuePath).sourceId(2).build()) {
                    EventHandler eventHandler = eventHandlerQueue
                            .createAppender()
                            .methodWriterBuilder(EventHandler.class)
                            .recordHistory(true)
                            .get();

                    CommandHandler commandHandler = new CommandHandler() {
                        private ConcertService concertService = new ConcertService(eventHandler);

                        @Override
                        public void onCreateConcert(CreateConcert createConcert) {
                            onCreateConcertCallsCount.getAndIncrement();
                            concertService.onCreateConcert(createConcert);
                        }

                        @Override
                        public void onTicketPurchase(TicketPurchase ticketPurchase) {
                            onTicketPurchaseCallsCount.getAndIncrement();
                            concertService.onTicketPurchase(ticketPurchase);
                        }
                    };

                    MethodReader createConcertReader = createConcertQueue
                            .createTailer()
                            .afterLastWritten(eventHandlerQueue)
                            .methodReader(commandHandler);

                    MethodReader commandHandlerReader = commandHandlerQueue
                            .createTailer()
                            .afterLastWritten(eventHandlerQueue)
                            .methodReader(commandHandler);

                    createConcertReader.readOne();
                    commandHandlerReader.readOne();
                }
            }
            assertEquals("Only " + numberOfEventsPerQueue + " call of onCreateConcert() expected", numberOfEventsPerQueue, onCreateConcertCallsCount.get());
            assertEquals("Only " + numberOfEventsPerQueue + " call of onTicketPurchase() expected", numberOfEventsPerQueue, onTicketPurchaseCallsCount.get());
        } finally {
            try {
                IOTools.shallowDeleteDirWithFiles(createConcertQueuePath);
                IOTools.shallowDeleteDirWithFiles(commandHandlerQueuePath);
                IOTools.shallowDeleteDirWithFiles(eventHandlerQueuePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
