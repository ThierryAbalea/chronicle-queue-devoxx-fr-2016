package com.github.thierryabalea.ticket_sales.main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import com.github.thierryabalea.ticket_sales.framework.Dispatcher;
import com.github.thierryabalea.ticket_sales.framework.Publisher;
import com.github.thierryabalea.ticket_sales.io.Journaller;
import com.lmax.disruptor.ClaimStrategy;
import com.lmax.disruptor.EventPublisher;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.WaitStrategy.Option;
import com.lmax.disruptor.dsl.Disruptor;
import com.github.thierryabalea.ticket_sales.api.Message;
import com.github.thierryabalea.ticket_sales.io.UdpDataSource;
import com.github.thierryabalea.ticket_sales.io.UdpEventHandler;

public class ConcertServiceMain
{
    public static final int SERVER_PORT = 50001;
    public static final int CLIENT_PORT = 50002;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException
    {
        Executor executor = Executors.newCachedThreadPool();
        Option waitStrategy = WaitStrategy.Option.BLOCKING;

        // Out bound Event Handling...
        Disruptor<Message> outboundDisruptor = new Disruptor<Message>(Message.FACTORY, 1024, executor,
                ClaimStrategy.Option.SINGLE_THREADED, waitStrategy);
        
        UdpEventHandler udpEventHandler = new UdpEventHandler("localhost", CLIENT_PORT);
        
        outboundDisruptor.handleEventsWith(udpEventHandler);
        RingBuffer<Message> outboundBuffer = outboundDisruptor.start();
        
        // In bound Event Handling
        Disruptor<Message> inboundDisruptor = new Disruptor<Message>(Message.FACTORY, 1024, executor,
                ClaimStrategy.Option.SINGLE_THREADED, waitStrategy);
        
        Journaller journaller = new Journaller(new File("/tmp"));
        
        Publisher publisher = new Publisher(new EventPublisher<Message>(outboundBuffer));
        ConcertService concertService = new ConcertService(publisher);
        Dispatcher dispatcher = new Dispatcher(concertService);
        
        inboundDisruptor.handleEventsWith(journaller).then(dispatcher);
        RingBuffer<Message> inboundBuffer = inboundDisruptor.start();
        
        // Data Source
        UdpDataSource udpDataSource = new UdpDataSource(inboundBuffer, SERVER_PORT);
        udpDataSource.bind();
        
        udpDataSource.run();
    }
}
