package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.ConcertFactory;
import com.github.thierryabalea.ticket_sales.api.command.TicketPurchase;
import com.github.thierryabalea.ticket_sales.api.event.AllocationApproved;
import com.github.thierryabalea.ticket_sales.api.event.AllocationRejected;
import com.github.thierryabalea.ticket_sales.api.event.ConcertCreated;
import com.github.thierryabalea.ticket_sales.api.event.SectionUpdated;
import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.JSONWire;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * inspire from
 * https://github.com/Vanilla-Java/Microservices/blob/master/src/test/java/net/openhft/samples/microservices/ComponentsBenchmark.java
 */
/*
  Percentiles, us/op:
      p(0.0000) =      2.908 us/op
     p(50.0000) =      3.072 us/op
     p(90.0000) =      3.460 us/op
     p(95.0000) =      3.992 us/op
     p(99.0000) =      4.536 us/op
     p(99.9000) =     13.280 us/op
     p(99.9900) =     17.804 us/op
     p(99.9990) =     81.746 us/op
     p(99.9999) =    880.761 us/op
    p(100.0000) =    896.000 us/op
 */

@State(Scope.Thread)
public class ComponentsBenchmark {

    private final JSONWire wire = new JSONWire(Bytes.elasticByteBuffer());
    private File commandHandlerQueuePath;
    private ChronicleQueue commandHandlerQueue;
    private File eventHandlerQueuePath;
    private ChronicleQueue eventHandlerQueue;
    private MethodReader eventHandlerReader;
    private MethodReader commandHandlerReader;
    private JsonRequestHandler requestHandler;
    private int counter = 0;

    public static void main(String[] args) throws RunnerException, InvocationTargetException, IllegalAccessException, IOException {
        ComponentsBenchmark main = new ComponentsBenchmark();
        if (Jvm.isFlightRecorder()) {
            // -verbose:gc  -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=dumponexit=true,filename=myrecording.jfr,settings=profile -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
            System.out.println("Detected Flight Recorder");
            main.setup();
            long start = System.currentTimeMillis();
            while (start + 60e3 > System.currentTimeMillis()) {
                for (int i = 0; i < 1000; i++)
                    main.benchmarkComponents();
            }
            main.tearDown();

        } else if (Jvm.isDebug()) {
            runAll(main, Setup.class);
            for (int i = 0; i < 10; i++) {
                runAll(main, Benchmark.class);
            }
            runAll(main, TearDown.class);

        } else {
            int time = Boolean.getBoolean("longTest") ? 30 : 6;
            System.out.println("measurementTime: " + time + " secs");
            Options opt = new OptionsBuilder()
                    .include(ComponentsBenchmark.class.getSimpleName())
                    .warmupIterations(10)
                    .forks(1)
                    .mode(Mode.SampleTime)
                    .measurementTime(TimeValue.seconds(time))
                    .timeUnit(TimeUnit.MICROSECONDS)
                    .build();

            new Runner(opt).run();
        }
    }

    private static void runAll(ComponentsBenchmark main, Class annotationClass) throws IllegalAccessException, InvocationTargetException {
        for (Method m : ComponentsBenchmark.class.getMethods())
            if (m.getAnnotation(annotationClass) != null)
                m.invoke(main);
    }

    @TearDown
    public void tearDown() {
        commandHandlerQueue.close();
        eventHandlerQueue.close();
        IOTools.shallowDeleteDirWithFiles(commandHandlerQueuePath);
        IOTools.shallowDeleteDirWithFiles(eventHandlerQueuePath);
    }

    boolean first = true;

    @Benchmark
    public void benchmarkComponents() {
        if (first && OS.isLinux()) {
            System.out.println();
            AffinityLock.acquireLock();
            System.out.println();
            first = false;
        }

        wire.clear();
        wire.write("accountId").int32(12);
        wire.write("requestId").int32(76);
        switch (counter++ & 3) {
            case 0:
                wire.write("concertId").int32(1);
                wire.write("numSeats").int32(1);
                wire.write("sectionId").int32(1);
                break;
            case 1:
                wire.write("concertId").int32(2);
                wire.write("numSeats").int32(8);
                wire.write("sectionId").int32(2);
                break;
            case 2:
                wire.write("concertId").int32(1);
                wire.write("numSeats").int32(3);
                wire.write("sectionId").int32(3);
                break;
            case 3:
                wire.write("concertId").int32(2);
                wire.write("numSeats").int32(20);
                wire.write("sectionId").int32(5);
                break;
        }

        requestHandler.onRequest(wire);

        // onTicketPurchase
        assertTrue(commandHandlerReader.readOne());
        // onSectionUpdated
        assertTrue(eventHandlerReader.readOne());
        // onAllocationApproved
        assertTrue(eventHandlerReader.readOne());
    }

    @Setup
    public void setup() throws IOException {
        String target = OS.TMP;

        // events
        eventHandlerQueuePath = new File(target, "ComponentsBenchmark-eventHandlerQueue-" + System.nanoTime());
        eventHandlerQueue = SingleChronicleQueueBuilder.binary(eventHandlerQueuePath).build();
        EventHandler eventHandler = eventHandlerQueue.createAppender().methodWriter(EventHandler.class);
        CommandHandler commandHandler = new ConcertService(eventHandler);

        EventHandler noOpEventHandler = new NoOpEventHandler();
        eventHandlerReader = eventHandlerQueue.createTailer().methodReader(noOpEventHandler);

        // commands
        commandHandlerQueuePath = new File(target, "ComponentsBenchmark-commandHandlerQueue-" + System.nanoTime());
        commandHandlerQueue = SingleChronicleQueueBuilder.binary(commandHandlerQueuePath).build();
        commandHandlerReader = commandHandlerQueue.createTailer().methodReader(commandHandler);

        CommandHandler commandHandlerProxy = commandHandlerQueue.createAppender().methodWriter(CommandHandler.class);

        TicketPurchase ticket = new TicketPurchase();
        requestHandler = request -> {
            ticket.readMarshallable(request);
            commandHandlerProxy.onTicketPurchase(ticket);
        };

        ConcertFactory.createConcerts().stream().forEachOrdered(commandHandlerProxy::onCreateConcert);
        assertTrue(commandHandlerReader.readOne());
        assertTrue(eventHandlerReader.readOne());
        assertTrue(commandHandlerReader.readOne());
        assertTrue(eventHandlerReader.readOne());

    }

    private interface JsonRequestHandler {
        void onRequest(JSONWire request);
    }

    private static class NoOpEventHandler implements EventHandler {

        @Override
        public void onConcertAvailable(ConcertCreated concertCreated) {
            // no op
        }

        @Override
        public void onAllocationApproved(AllocationApproved allocationApproved) {
            // no op
        }

        @Override
        public void onAllocationRejected(AllocationRejected allocationRejected) {
            // no op
        }

        @Override
        public void onSectionUpdated(SectionUpdated sectionUpdated) {
            // no op
        }
    }

    ;
}
