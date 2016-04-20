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

    public static void main(String[] args) throws Exception {

        String eventHandlerQueuePath = format("%s/%s", OS.TARGET, "eventHandlerQueue");
        ChronicleQueue eventHandlerQueue = SingleChronicleQueueBuilder.binary(eventHandlerQueuePath).build();
        EventHandler eventHandler = eventHandlerQueue
                .createAppender()
                .methodWriterBuilder(EventHandler.class)
                .get();

        CommandHandler commandHandler = new ConcertService(eventHandler);

        String commandHandlerQueuePath = format("%s/%s", OS.TARGET, "commandHandlerQueue");
        ChronicleQueue commandHandlerQueue = SingleChronicleQueueBuilder.binary(commandHandlerQueuePath).build();
        MethodReader commandHandlerReader = commandHandlerQueue
                .createTailer()
                .methodReader(commandHandler);

        String createConcertQueuePath = format("%s/%s", OS.TARGET, "createConcertQueue");
        ChronicleQueue createConcertQueue = SingleChronicleQueueBuilder.binary(createConcertQueuePath).build();
        MethodReader createConcertReader = createConcertQueue
                .createTailer()
                .methodReader(commandHandler);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                eventHandlerQueue.close();
                commandHandlerQueue.close();
                createConcertQueue.close();
            }
        });

        new Thread(() -> {
            AffinityLock lock = AffinityLock.acquireLock();
            try {
                Pauser pauser = new LongPauser(1, 100, 500, 10_000, MICROSECONDS);
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
        }).start();
    }
}
