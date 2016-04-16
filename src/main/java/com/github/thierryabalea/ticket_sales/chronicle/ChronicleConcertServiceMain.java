package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.threads.LongPauser;
import net.openhft.chronicle.threads.Pauser;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MICROSECONDS;

public class ChronicleConcertServiceMain extends Thread {

    private final Pauser pauser = new LongPauser(1, 100, 500, 10_000, MICROSECONDS);
    private MethodReader commandHandlerReader;
    private MethodReader createConcertReader;

    public static void main(String[] args) throws Exception {
        new ChronicleConcertServiceMain().main();
    }

    public void main() {
        String commandHandlerQueue = format("%s/%s", OS.TARGET, "commandHandlerQueue");
        String eventHandlerQueue = format("%s/%s", OS.TARGET, "eventHandlerQueue");
        String createConcertQueue = format("%s/%s", OS.TARGET, "createConcertQueue");

        CommandHandler commandHandler;

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(eventHandlerQueue).build()) {
            EventHandler eventHandler = queue.createAppender()
                    .methodWriterBuilder(EventHandler.class)
                    .recordHistory(true)
                    .get();
            commandHandler = new ConcertService(eventHandler);
        }

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(commandHandlerQueue).build()) {
            commandHandlerReader = queue.createTailer().afterLastWritten(queue).methodReader(commandHandler);
        }

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(createConcertQueue).build()) {
            createConcertReader = queue.createTailer().afterLastWritten(queue).methodReader(commandHandler);
        }

        this.start();
    }

    @Override
    public void run() {
        AffinityLock lock = AffinityLock.acquireLock();
        try {
            while (true) {
                boolean didSomeWork = false;

                didSomeWork |= commandHandlerReader.readOne();
                didSomeWork |= createConcertReader.readOne();

                if (didSomeWork) {
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
