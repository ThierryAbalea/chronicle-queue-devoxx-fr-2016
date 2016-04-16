package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.api.Poll;
import com.github.thierryabalea.ticket_sales.api.TicketPurchase;
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

    private final Pauser pauser = new LongPauser(1, 100, 500, 10_000, MICROSECONDS);
    private MpscArrayQueue<Poll> pollQueue;
    private ResponseWebServer responseWebServer;
    private MethodReader eventHandlerReader;

    public static void main(String[] args) throws Exception {
        new ChronicleWebMain().main();
    }

    public void main() throws Exception {
        On.port(7070);

        pollQueue = new MpscArrayQueue<>(1024);
        responseWebServer = new ResponseWebServer();
        ResponseWebServer.PollHandler pollHandler = (accountId, version) -> pollQueue.offer(new Poll(accountId, version));
        responseWebServer.init(pollHandler);

        String commandHandlerQueue = format("%s/%s", OS.TARGET, "commandHandlerQueue");
        String eventHandlerQueue = format("%s/%s", OS.TARGET, "eventHandlerQueue");

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(commandHandlerQueue).build()) {
            CommandHandler commandHandler = queue.createAppender()
                    .methodWriterBuilder(CommandHandler.class)
                    .recordHistory(true)
                    .get();

            ExecutorService executorService = newSingleThreadExecutor();
            RequestWebServer.JsonRequestHandler requestHandler = request -> executorService.execute(() -> {
                TicketPurchase ticketPurchase = TicketPurchaseFromJson.fromJson(request);
                commandHandler.onTicketPurchase(ticketPurchase);
            });

            RequestWebServer requestWebServer = new RequestWebServer(requestHandler);
            requestWebServer.init();
        }

        try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary(eventHandlerQueue).build()) {
            eventHandlerReader = queue.createTailer().afterLastWritten(queue).methodReader(responseWebServer);
        }

        this.start();
    }

    @Override
    public void run() {
        AffinityLock lock = AffinityLock.acquireLock();
        try {
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
    }
}