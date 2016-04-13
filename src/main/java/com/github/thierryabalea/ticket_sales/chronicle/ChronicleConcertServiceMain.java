package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import com.github.thierryabalea.ticket_sales.domain.ConcertServiceListener;
import com.github.thierryabalea.ticket_sales.domain.ConcertServiceManager;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

import static java.lang.String.format;

public class ChronicleConcertServiceMain {

    public static void main(String[] args) throws Exception {
        String concertServiceQueue = format("%s/%s", OS.TARGET, "concertServiceQueue");
        String concertServiceListenerQueue = format("%s/%s", OS.TARGET, "concertServiceListenerQueue");

        ConcertService concertService;

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(concertServiceListenerQueue).build()) {
            ConcertServiceListener serviceListener = queue.createAppender().methodWriter(ConcertServiceListener.class);
            concertService = new ConcertServiceManager(serviceListener);
        }

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(concertServiceQueue).build()) {
            MethodReader reader = queue.createTailer().methodReader(concertService);
            Thread controller = new ControllerThread(reader);
            controller.run();
        }
    }
}
