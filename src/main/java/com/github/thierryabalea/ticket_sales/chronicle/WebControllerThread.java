package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.api.Poll;
import com.github.thierryabalea.ticket_sales.web.ResponseWebServer;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.queue.MethodReader;
import org.jctools.queues.MpscArrayQueue;

public class WebControllerThread extends Thread {
    private final MethodReader reader;
    private MpscArrayQueue<Poll> pollQueue;
    private ResponseWebServer.PollHandler pollHandler;

    public WebControllerThread(MethodReader reader, MpscArrayQueue<Poll> pollQueue, ResponseWebServer.PollHandler pollHandler) {
        this.reader = reader;
        this.pollQueue = pollQueue;
        this.pollHandler = pollHandler;
    }

    @Override
    public void run() {
        AffinityLock lock = AffinityLock.acquireLock();
        try {
            while (true) {
                reader.readOne();

                Poll poll = pollQueue.poll();
                if (poll != null) {
                    pollHandler.onPoll(poll.accountId, poll.version);
                }
            }
        } finally {
            lock.release();
        }
    }
}
