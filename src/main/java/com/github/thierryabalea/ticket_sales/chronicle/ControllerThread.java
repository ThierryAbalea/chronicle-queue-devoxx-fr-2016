package com.github.thierryabalea.ticket_sales.chronicle;

import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.queue.MethodReader;

public class ControllerThread extends Thread {
    private final MethodReader reader;

    public ControllerThread(MethodReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {
        AffinityLock lock = AffinityLock.acquireLock();
        try {
            while (true) {
                reader.readOne();
            }
        } finally {
            lock.release();
        }
    }
}
