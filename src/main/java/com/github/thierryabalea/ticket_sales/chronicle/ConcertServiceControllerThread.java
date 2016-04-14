package com.github.thierryabalea.ticket_sales.chronicle;

import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.queue.MethodReader;

public class ConcertServiceControllerThread extends Thread {
    private final MethodReader concertServiceReader;
    private final MethodReader concertCreatedReader;

    public ConcertServiceControllerThread(MethodReader concertServiceReader, MethodReader concertCreatedReader) {
        this.concertServiceReader = concertServiceReader;
        this.concertCreatedReader = concertCreatedReader;
    }

    @Override
    public void run() {
        AffinityLock lock = AffinityLock.acquireLock();
        try {
            while (true) {
                concertServiceReader.readOne();
                concertCreatedReader.readOne();
            }
        } finally {
            lock.release();
        }
    }
}
