package com.github.thierryabalea.ticket_sales.udp;

import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.io.File;
import java.io.IOException;

public class UDPConcertServiceMain {
    public static final int SERVER_PORT = 50001;
    public static final int CLIENT_PORT = 50002;

    public static void main(String[] args) throws IOException {
        // Out bound Event Handling...
        Disruptor<Message> outboundDisruptor = new Disruptor<>(
                Message.FACTORY,
                1024,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE, // Single producer
                new BlockingWaitStrategy());

        UdpEventHandler udpEventHandler = new UdpEventHandler("localhost", CLIENT_PORT);

        outboundDisruptor.handleEventsWith(udpEventHandler);
        RingBuffer<Message> outboundBuffer = outboundDisruptor.start();

        // In bound Event Handling
        Disruptor<Message> inboundDisruptor = new Disruptor<>(
                Message.FACTORY,
                1024,
                DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE, // Single producer
                new BlockingWaitStrategy());
        Journaller journaller = new Journaller(new File("/tmp"));

        Publisher publisher = new Publisher(outboundBuffer);
        ConcertService concertService = new ConcertService(publisher);
        Dispatcher dispatcher = new Dispatcher(concertService);

        //noinspection unchecked
        inboundDisruptor.handleEventsWith(journaller).then(dispatcher::onEvent);
        RingBuffer<Message> inboundBuffer = inboundDisruptor.start();

        // Data Source
        UdpDataSource udpDataSource = new UdpDataSource(inboundBuffer, SERVER_PORT);
        udpDataSource.bind();

        udpDataSource.run();
    }
}
