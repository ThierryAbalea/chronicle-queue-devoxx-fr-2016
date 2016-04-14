package com.github.thierryabalea.ticket_sales.chronicle;

import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.queue.MethodReader;

public class ConcertServiceControllerThread extends Thread {
    private final MethodReader concertServiceReader;
    private final MethodReader createConcertReader;

    public ConcertServiceControllerThread(MethodReader concertServiceReader, MethodReader createConcertReader) {
        this.concertServiceReader = concertServiceReader;
        this.createConcertReader = createConcertReader;
    }

    @Override
    public void run() {
        AffinityLock lock = AffinityLock.acquireLock();
        try {
            while (true) {
                concertServiceReader.readOne();
                createConcertReader.readOne();
            }
        } finally {
            lock.release();
        }
    }
}
