package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.api.Poll;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import com.github.thierryabalea.ticket_sales.web.RequestWebServer;
import com.github.thierryabalea.ticket_sales.web.ResponseWebServer;
import com.github.thierryabalea.ticket_sales.web.json.TicketPurchaseFromJson;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import org.jctools.queues.MpscArrayQueue;
import org.rapidoid.http.fast.On;

import java.util.concurrent.ExecutorService;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class ChronicleWebMain {

    public static void main(String[] args) throws Exception {
        On.port(7070);

        MpscArrayQueue<Poll> pollQueue = new MpscArrayQueue<>(1024);
        ResponseWebServer responseWebServer = new ResponseWebServer();
        ResponseWebServer.PollHandler pollHandler = (accountId, version) -> pollQueue.offer(new Poll(accountId, version));
        responseWebServer.init(pollHandler);

        String concertServiceQueue = format("%s/%s", OS.TARGET, "concertServiceQueue");
        String concertServiceListenerQueue = format("%s/%s", OS.TARGET, "concertServiceListenerQueue");

        ExecutorService executorService = newSingleThreadExecutor();
        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(concertServiceQueue).build()) {
            ConcertService concertService = queue.createAppender()
                    .methodWriterBuilder(ConcertService.class)
                    .recordHistory(true)
                    .get();

            RequestWebServer.JsonRequestHandler requestHandler = request -> {
                executorService.execute(() -> {
                    TicketPurchase ticketPurchase = TicketPurchaseFromJson.fromJson(request);
                    concertService.onTicketPurchase(ticketPurchase);
                });
            };

            RequestWebServer requestWebServer = new RequestWebServer(requestHandler);
            requestWebServer.init();
        }

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(concertServiceListenerQueue).build()) {
            MethodReader reader = queue.createTailer().methodReader(responseWebServer);
            Thread controller = new WebControllerThread(reader, pollQueue, responseWebServer::onPoll);
            controller.run();
        }
    }
}