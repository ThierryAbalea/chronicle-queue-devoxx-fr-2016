package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
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
        String commandHandlerQueue = format("%s/%s", OS.TARGET, "commandHandlerQueue");
        String eventHandlerQueue = format("%s/%s", OS.TARGET, "eventHandlerQueue");
        String concertCreatedQueue = format("%s/%s", OS.TARGET, "concertCreatedQueue");

        CommandHandler commandHandler;

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(eventHandlerQueue).build()) {
            EventHandler eventHandler = queue.createAppender()
                    .methodWriterBuilder(EventHandler.class)
                    .recordHistory(true)
                    .get();
            commandHandler = new ConcertServiceManager(eventHandler);
        }

        ExecutorService executorService = newSingleThreadExecutor();

        MethodReader commandHandlerReader;
        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(commandHandlerQueue).build()) {
            commandHandlerReader = queue.createTailer().afterLastWritten(queue).methodReader(commandHandler);
        }

        MethodReader concertCreatedReader;
        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(concertCreatedQueue).build()) {
            concertCreatedReader = queue.createTailer().afterLastWritten(queue).methodReader(commandHandler);
        }

        executorService.execute(new ConcertServiceControllerThread(commandHandlerReader, concertCreatedReader));
    }
}
