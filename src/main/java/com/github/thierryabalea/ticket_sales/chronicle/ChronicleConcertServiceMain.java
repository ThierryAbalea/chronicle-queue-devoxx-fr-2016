package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import com.github.thierryabalea.ticket_sales.domain.ConcertServiceListener;
import com.github.thierryabalea.ticket_sales.domain.ConcertServiceManager;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import java.util.concurrent.ExecutorService;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class ChronicleConcertServiceMain {

    public static void main(String[] args) throws Exception {
        String concertServiceQueue = format("%s/%s", OS.TARGET, "concertServiceQueue");
        String concertServiceListenerQueue = format("%s/%s", OS.TARGET, "concertServiceListenerQueue");
        String concertCreatedQueue = format("%s/%s", OS.TARGET, "concertCreatedQueue");

        ConcertService concertService;

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(concertServiceListenerQueue).build()) {
            ConcertServiceListener serviceListener = queue.createAppender()
                    .methodWriterBuilder(ConcertServiceListener.class)
                    .recordHistory(true)
                    .get();
            concertService = new ConcertServiceManager(serviceListener);
        }

        ExecutorService executorService = newFixedThreadPool(2);

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(concertServiceQueue).build()) {
            MethodReader reader = queue.createTailer().afterLastWritten(queue).methodReader(concertService);
            executorService.execute(new ControllerThread(reader));
        }

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(concertCreatedQueue).build()) {
            MethodReader reader = queue.createTailer().afterLastWritten(queue).methodReader(concertService);
            executorService.execute(new ControllerThread(reader));
        }
    }
}
