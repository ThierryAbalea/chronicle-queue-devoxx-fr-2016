package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import com.github.thierryabalea.ticket_sales.domain.EventHandler;
import com.github.thierryabalea.ticket_sales.domain.ConcertServiceManager;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import java.util.concurrent.ExecutorService;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class ChronicleConcertServiceMain {

    public static void main(String[] args) throws Exception {
        String concertServiceQueue = format("%s/%s", OS.TARGET, "concertServiceQueue");
        String eventHandlerQueue = format("%s/%s", OS.TARGET, "eventHandlerQueue");
        String concertCreatedQueue = format("%s/%s", OS.TARGET, "concertCreatedQueue");

        ConcertService concertService;

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(eventHandlerQueue).build()) {
            EventHandler eventHandler = queue.createAppender()
                    .methodWriterBuilder(EventHandler.class)
                    .recordHistory(true)
                    .get();
            concertService = new ConcertServiceManager(eventHandler);
        }

        ExecutorService executorService = newSingleThreadExecutor();

        MethodReader concertServiceReader;
        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(concertServiceQueue).build()) {
            concertServiceReader = queue.createTailer().afterLastWritten(queue).methodReader(concertService);
        }

        MethodReader concertCreatedReader;
        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(concertCreatedQueue).build()) {
            concertCreatedReader = queue.createTailer().afterLastWritten(queue).methodReader(concertService);
        }

        executorService.execute(new ConcertServiceControllerThread(concertServiceReader, concertCreatedReader));
    }
}
