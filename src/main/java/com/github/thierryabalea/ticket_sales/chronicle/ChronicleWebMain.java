package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.api.command.Poll;
import com.github.thierryabalea.ticket_sales.api.command.TicketPurchase;
import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;
import com.github.thierryabalea.ticket_sales.web.RequestWebServer;
import com.github.thierryabalea.ticket_sales.web.ResponseWebServer;
import com.github.thierryabalea.ticket_sales.web.json.TicketPurchaseFromJson;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.threads.LongPauser;
import net.openhft.chronicle.threads.Pauser;
import org.jctools.queues.MpscArrayQueue;
import org.rapidoid.http.fast.On;

import java.util.concurrent.ExecutorService;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MICROSECONDS;

public class ChronicleWebMain extends Thread {

    public static void main(String[] args) throws Exception {

        On.port(7070);

        MpscArrayQueue<Poll> pollQueue = new MpscArrayQueue<>(1024);
        ResponseWebServer responseWebServer = new ResponseWebServer();
        ResponseWebServer.PollHandler pollHandler = (accountId, version) -> pollQueue.offer(new Poll(accountId, version));
        responseWebServer.init(pollHandler);

        String commandHandlerQueuePath = format("%s/%s", OS.TARGET, "commandHandlerQueue");
        ChronicleQueue commandHandlerQueue = SingleChronicleQueueBuilder.binary(commandHandlerQueuePath).build();
        CommandHandler commandHandler = commandHandlerQueue
                .createAppender()
                .methodWriterBuilder(CommandHandler.class)
                .get();

        ExecutorService executorService = newSingleThreadExecutor();
        RequestWebServer.JsonRequestHandler requestHandler = request -> executorService.execute(() -> {
            TicketPurchase ticketPurchase = TicketPurchaseFromJson.fromJson(request);
            commandHandler.onTicketPurchase(ticketPurchase);
        });

        RequestWebServer requestWebServer = new RequestWebServer(requestHandler);
        requestWebServer.init();

        String eventHandlerQueuePath = format("%s/%s", OS.TARGET, "eventHandlerQueue");
        ChronicleQueue eventHandlerQueue = SingleChronicleQueueBuilder.binary(eventHandlerQueuePath).build();
        MethodReader eventHandlerReader = eventHandlerQueue
                .createTailer()
                .methodReader(responseWebServer);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                eventHandlerQueue.close();
                commandHandlerQueue.close();
            }
        });

        new Thread(() -> {
            AffinityLock lock = AffinityLock.acquireLock();
            try {
                Pauser pauser = new LongPauser(1, 100, 500, 10_000, MICROSECONDS);
                while (true) {
                    boolean didSomeWork;

                    didSomeWork = eventHandlerReader.readOne();

                    Poll poll = pollQueue.poll();
                    if (poll != null) {
                        responseWebServer.onPoll(poll.accountId, poll.version);
                        didSomeWork = true;
                    }

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