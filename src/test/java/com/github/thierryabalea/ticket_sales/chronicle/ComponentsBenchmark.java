package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.ConcertFactory;
import com.github.thierryabalea.ticket_sales.api.command.TicketPurchase;
import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import com.github.thierryabalea.ticket_sales.web.ResponseWebServer;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.JSONWire;
import org.openjdk.jmh.annotations.*;
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
      p(0.0000) =      3.740 us/op
     p(50.0000) =      3.872 us/op
     p(90.0000) =      4.224 us/op
     p(95.0000) =      4.880 us/op
     p(99.0000) =      5.296 us/op
     p(99.9000) =     13.536 us/op
     p(99.9900) =     18.385 us/op
     p(99.9990) =     33.549 us/op
     p(99.9999) =  11616.256 us/op
    p(100.0000) =  11616.256 us/op
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
        if (OS.isLinux()) {
            AffinityLock.acquireLock();
        }
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
            int time = Boolean.getBoolean("longTest") ? 30 : 3;
            System.out.println("measurementTime: " + time + " secs");
            Options opt = new OptionsBuilder()
                    .include(ComponentsBenchmark.class.getSimpleName())
                    .warmupIterations(8)
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

    @Benchmark
    public void benchmarkComponents() {
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

        ResponseWebServer responseWebServer = new ResponseWebServer();
        eventHandlerReader = eventHandlerQueue.createTailer().methodReader(responseWebServer);

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

    public interface JsonRequestHandler {
        void onRequest(JSONWire request);
    }
}
