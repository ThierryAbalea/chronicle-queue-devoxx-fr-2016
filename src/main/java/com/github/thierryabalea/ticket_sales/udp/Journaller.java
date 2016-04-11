package com.github.thierryabalea.ticket_sales.udp;

import com.lmax.disruptor.EventHandler;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Journaller implements EventHandler<Message> {
    private final File directory;
    private FileChannel file = null;
    private final ByteBuffer[] buffers = new ByteBuffer[2];

    public Journaller(File directory) {
        this.directory = directory;
        buffers[0] = ByteBuffer.allocate(4);
    }

    public void onEvent(Message event, long sequence, boolean endOfBatch) throws Exception {


        Bytes<ByteBuffer> byteBuffer = Bytes.elasticByteBuffer();
        Wire wire = new TextWire(byteBuffer);

        wire.getValueOut().object(event);
        System.out.println(byteBuffer);
        ByteBuffer buffer = ByteBuffer.wrap(byteBuffer.toByteArray());


        int size = byteBuffer.length();

        if (null == file) {
            file = new RandomAccessFile(new File(directory, "jnl"), "rw").getChannel();
        }

        buffers[0].clear();
        buffers[0].putInt(size).flip();
        buffers[1] = buffer;
        buffers[1].clear().limit(size);

        while (buffers[1].hasRemaining()) {
            file.write(buffers);
        }

        buffers[1].clear();
        buffers[1] = null;

        if (endOfBatch) {
            file.force(true);
        }
    }
}
