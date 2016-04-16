package com.github.thierryabalea.ticket_sales.udp;

import com.lmax.disruptor.RingBuffer;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpDataSource implements Runnable {
    private final RingBuffer<Message> ringBuffer;
    private final SocketAddress address;
    private DatagramChannel channel;
    private DatagramSocket socket;

    public UdpDataSource(RingBuffer<Message> ringBuffer, int port) {
        this.ringBuffer = ringBuffer;
        this.address = new InetSocketAddress(port);
    }

    public void bind() throws IOException {
        System.out.println("Binding to address: " + address);
        channel = DatagramChannel.open();
        socket = channel.socket();
        socket.bind(address);
    }

    public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(14000);
        ByteBuffer slice = buffer.slice();

        Thread t = Thread.currentThread();
        try {
            while (!t.isInterrupted()) {
                buffer.clear();
                slice.clear();

                channel.receive(buffer);
                buffer.flip();

                do {
                    int length = buffer.getInt(slice.position());
                    slice.position(slice.position() + 4);
                    slice.limit(slice.position() + length);

                    long sequence = ringBuffer.next();
                    Message message = ringBuffer.get(sequence);

                    Wire wire = new TextWire(Bytes.wrapForRead(slice));
                    slice.position(slice.position() + length);

                    message.readMarshallable(wire);

                    ringBuffer.publish(sequence);

                    slice.limit(buffer.limit());
                }
                while (slice.position() < buffer.limit());
            }
        } catch (IOException e) {
            System.err.println("Buffer receive failed, exiting...");
            e.printStackTrace();
        }
    }
}
