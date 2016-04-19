package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.ConcertFactory;
import com.github.thierryabalea.ticket_sales.api.command.TicketPurchase;
import com.github.thierryabalea.ticket_sales.api.event.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.event.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.event.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.event.SectionUpdated;
import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.core.jlbh.JLBH;
import net.openhft.chronicle.core.jlbh.JLBHOptions;
import net.openhft.chronicle.core.jlbh.JLBHTask;
import net.openhft.chronicle.core.util.NanoSampler;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.threads.LongPauser;
import net.openhft.chronicle.threads.Pauser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ThroughputBenchmark implements JLBHTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThroughputBenchmark.class);

    private static final int THROUGHPUT = Integer.getInteger("message.throughput", 400_000);
    private static final int MESSAGE_COUNT = Integer.getInteger("message.count", THROUGHPUT * 120);
    private static final String QUEUE_DIRECTORY_PATH = System.getProperty("queues.dir.path", OS.TMP + "/ThroughputBenchmark");
    private static final boolean ACCOUNT_FOR_COORDINATED_OMMISSION = true;

    private final UUID uuid = UUID.randomUUID();
    private final String eventHandlerQueuePath = QUEUE_DIRECTORY_PATH + "/" +  uuid + "/eventHandlerQueue";
    private final String commandHandlerQueuePath = QUEUE_DIRECTORY_PATH + "/" +  uuid + "/commandHandlerQueuePath";
    private final TicketPurchase ticketPurchase = new TicketPurchase(1, 1, 1, 12, 76);
    private EventHandlerSampler eventHandlerSampler;
    private CommandHandler commandHandler;

    public static void main(String[] args) {
        LOGGER.info("queues directory path (system properties 'queues.dir.path'): {}", QUEUE_DIRECTORY_PATH);
        LOGGER.info("throughput (system properties 'message.throughput'): {}", THROUGHPUT);
        LOGGER.info("number of ticket purchase requests (system properties 'message.count') : {}", MESSAGE_COUNT);
        JLBHOptions lth = new JLBHOptions()
                .warmUpIterations(50_000)
                .iterations(MESSAGE_COUNT)
                .throughput(THROUGHPUT)
                .runs(6)
                .recordOSJitter(true)
                .pauseAfterWarmupMS(500)
                .accountForCoordinatedOmmission(ACCOUNT_FOR_COORDINATED_OMMISSION)
                .jlbhTask(new ThroughputBenchmark());
        new JLBH(lth).start();
    }

    @Override
    public void run(long startTimeNS) {
        eventHandlerSampler.ts0 = System.nanoTime();
        commandHandler.onTicketPurchase(ticketPurchase);
    }

    @Override
    public void init(JLBH jlbh) {
        Thread serviceThread = new ChronicleConcertService();
        serviceThread.setDaemon(true);
        serviceThread.start();

        eventHandlerSampler = new EventHandlerSampler(jlbh);

        Thread webThread = new ChronicleWeb(eventHandlerSampler);
        webThread.setDaemon(true);
        webThread.start();

        ChronicleQueue commandHandlerQueue = SingleChronicleQueueBuilder.binary(commandHandlerQueuePath).build();
        commandHandler = commandHandlerQueue.createAppender().methodWriter(CommandHandler.class);
    }

    @Override
    public void complete() {
        IOTools.deleteDirWithFiles(eventHandlerQueuePath, 2);
        IOTools.deleteDirWithFiles(commandHandlerQueuePath, 2);
    }

    private class ChronicleConcertService extends Thread {
        private final Pauser pauser = new LongPauser(1, 100, 500, 10_000, TimeUnit.MICROSECONDS);
        private final MethodReader commandHandlerReader;

        ChronicleConcertService() {
            ChronicleQueue eventHandlerQueue = SingleChronicleQueueBuilder.binary(eventHandlerQueuePath).build();
            EventHandler eventHandler = eventHandlerQueue.createAppender().methodWriter(EventHandler.class);

            CommandHandler commandHandler = new ConcertService(eventHandler);
            ConcertFactory.createConcerts().stream().forEachOrdered(commandHandler::onCreateConcert);

            ChronicleQueue commandHandlerQueue = SingleChronicleQueueBuilder.binary(commandHandlerQueuePath).build();
            commandHandlerReader = commandHandlerQueue.createTailer().methodReader(commandHandler);
        }

        @Override
        public void run() {
            AffinityLock lock = AffinityLock.acquireLock();
            try {
                while (true) {
                    if (commandHandlerReader.readOne()) {
                        pauser.reset();
                    } else {
                        pauser.pause();
                    }
                }
            } finally {
                lock.release();
            }
        }
    }

    private class ChronicleWeb extends Thread {
        private final Pauser pauser = new LongPauser(1, 100, 500, 10_000, TimeUnit.MICROSECONDS);
        private final MethodReader eventHandlerReader;

        ChronicleWeb(EventHandlerSampler eventHandlerSampler) {
            ChronicleQueue eventHandlerQueue = SingleChronicleQueueBuilder.binary(eventHandlerQueuePath).build();
            eventHandlerReader = eventHandlerQueue.createTailer().methodReader(eventHandlerSampler);
        }

        @Override
        public void run() {
            AffinityLock lock = AffinityLock.acquireLock();
            try {
                while (true) {
                    if (eventHandlerReader.readOne()) {
                        pauser.reset();
                    } else {
                        pauser.pause();
                    }
                }
            } finally {
                lock.release();
            }
        }
    }

    private class EventHandlerSampler implements EventHandler {

        private final NanoSampler endToEnd;
        long ts0;

        public EventHandlerSampler(NanoSampler endToEnd) {
            this.endToEnd = endToEnd;
        }

        @Override
        public void onConcertAvailable(ConcertCreated concertCreated) {
        }

        @Override
        public void onAllocationApproved(AllocationApproved allocationApproved) {
            // this sample include at this stage also the consuming of onConcertAvailable call
            // (because onConcertAvailable is called/published before onAllocationApproved)
            long time = System.nanoTime() - ts0;
            endToEnd.sampleNanos(time);
        }

        @Override
        public void onAllocationRejected(AllocationRejected allocationRejected) {
        }

        @Override
        public void onSectionUpdated(SectionUpdated sectionUpdated) {
        }
    }
}
