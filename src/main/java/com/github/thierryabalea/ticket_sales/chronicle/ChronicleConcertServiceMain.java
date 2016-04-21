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

import java.util.concurrent.TimeUnit;

public class ChronicleConcertServiceMain extends Thread {

    public static void main(String[] args) throws Exception {

        String eventHandlerQueuePath = String.format("%s/%s", OS.TARGET, "eventHandlerQueue");
        ChronicleQueue eventHandlerQueue = SingleChronicleQueueBuilder.binary(eventHandlerQueuePath).build();
        EventHandler eventHandler = eventHandlerQueue
                .createAppender()
                .methodWriterBuilder(EventHandler.class)
                .get();

        CommandHandler commandHandler = new ConcertService(eventHandler);

        String createConcertQueuePath = String.format("%s/%s", OS.TARGET, "createConcertQueue");
        ChronicleQueue createConcertQueue = SingleChronicleQueueBuilder.binary(createConcertQueuePath).build();
        MethodReader createConcertReader = createConcertQueue
                .createTailer()
                .methodReader(commandHandler);

        String commandHandlerQueuePath = String.format("%s/%s", OS.TARGET, "commandHandlerQueue");
        ChronicleQueue commandHandlerQueue = SingleChronicleQueueBuilder.binary(commandHandlerQueuePath).build();
        MethodReader ticketPurchaseReader = commandHandlerQueue
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
                Pauser pauser = new LongPauser(1, 100, 500, 10_000, TimeUnit.MICROSECONDS);
                while (true) {
                    boolean didSomeWork = false;

                    didSomeWork |= ticketPurchaseReader.readOne();
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
