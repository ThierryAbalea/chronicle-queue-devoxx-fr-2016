package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.ConcertFactory;
import com.github.thierryabalea.ticket_sales.api.command.TicketPurchase;
import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import com.github.thierryabalea.ticket_sales.web.ResponseWebServer;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.core.jlbh.JLBH;
import net.openhft.chronicle.core.jlbh.JLBHOptions;
import net.openhft.chronicle.core.jlbh.JLBHTask;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.threads.LongPauser;
import net.openhft.chronicle.threads.Pauser;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ThroughputBenchmark implements JLBHTask {

    private static final int THROUGHPUT = Integer.getInteger("message.throughput", 400_000);
    private static final int MESSAGE_COUNT = Integer.getInteger("message.count", THROUGHPUT * 120);
    private static final boolean ACCOUNT_FOR_COORDINATED_OMMISSION = true;

    private final UUID uuid = UUID.randomUUID();
    private final String eventHandlerQueuePath = OS.TMP + "ThroughputBenchmark/" + uuid + "/eventHandlerQueue";
    private final String commandHandlerQueuePath = OS.TMP + "ThroughputBenchmark/" + uuid + "/commandHandlerQueuePath";
    private final TicketPurchase ticketPurchase = new TicketPurchase(1, 1, 1, 12, 76);
    private EventHandler responseWebServer = new ResponseWebServer();
    private ChronicleConcertService chronicleConcertService;
    private ChronicleWeb chronicleWeb;


    public static void main(String[] args) {
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
        CommandHandler commandHandler = chronicleConcertService.commandHandler;
        EventHandlerSampler eventHandlerSampler = chronicleWeb.eventHandlerSampler;
        eventHandlerSampler.ts0 = System.nanoTime();
        commandHandler.onTicketPurchase(ticketPurchase);
    }

    @Override
    public void init(JLBH jlbh) {
        chronicleConcertService = new ChronicleConcertService();
        chronicleWeb = new ChronicleWeb(jlbh);
    }

    @Override
    public void complete() {
        IOTools.deleteDirWithFiles(eventHandlerQueuePath, 2);
        IOTools.deleteDirWithFiles(commandHandlerQueuePath, 2);
    }

    private class ChronicleConcertService extends Thread {
        private final Pauser pauser = new LongPauser(1, 100, 500, 10_000, TimeUnit.MICROSECONDS);
        private final MethodReader commandHandlerReader;
        private final EventHandler eventHandler;
        private final CommandHandler commandHandler;

        private ChronicleConcertService() {
            try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(eventHandlerQueuePath).build()) {
                eventHandler = queue.createAppender().methodWriter(EventHandler.class);
            }

            try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(commandHandlerQueuePath).build()) {
                commandHandler = new ConcertService(eventHandler);
                ConcertFactory.createConcerts().stream().forEachOrdered(commandHandler::onCreateConcert);
                commandHandlerReader = queue.createTailer().methodReader(commandHandler);
            }
            this.setDaemon(true);
            this.start();
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
        private final CommandHandler commandHandler;
        private final EventHandlerSampler eventHandlerSampler;

        private ChronicleWeb(JLBH jlbh) {
            try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(commandHandlerQueuePath).build()) {
                commandHandler = queue.createAppender().methodWriter(CommandHandler.class);
            }

            try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(eventHandlerQueuePath).build()) {
                eventHandlerSampler = new EventHandlerSampler(responseWebServer, jlbh);
                eventHandlerReader = queue.createTailer().methodReader(eventHandlerSampler);
            }

            this.setDaemon(true);
            this.start();
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
}
