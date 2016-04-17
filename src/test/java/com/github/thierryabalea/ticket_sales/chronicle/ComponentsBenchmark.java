package com.github.thierryabalea.ticket_sales.chronicle;

import com.github.thierryabalea.ticket_sales.ConcertFactory;
import com.github.thierryabalea.ticket_sales.api.command.TicketPurchase;
import com.github.thierryabalea.ticket_sales.api.service.CommandHandler;
import com.github.thierryabalea.ticket_sales.api.service.EventHandler;
import com.github.thierryabalea.ticket_sales.domain.ConcertService;
import com.github.thierryabalea.ticket_sales.web.ResponseWebServer;
import net.openhft.affinity.AffinityLock;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
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

import static com.github.thierryabalea.ticket_sales.api.command.TicketPurchase.newTicketPurchase;
import static org.junit.Assert.assertTrue;

/**
 * inspire from
 * https://github.com/Vanilla-Java/Microservices/blob/master/src/test/java/net/openhft/samples/microservices/ComponentsBenchmark.java
 */

@State(Scope.Thread)
public class ComponentsBenchmark {

    private File commandHandlerQueuePath;
    private ChronicleQueue commandHandlerQueue;
    private File eventHandlerQueuePath;
    private ChronicleQueue eventHandlerQueue;
    private MethodReader eventHandlerReader;
    private MethodReader commandHandlerReader;
    private int counter = 0;
    private CommandHandler commandHandlerProxy;

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
            for (int i = 0; i < 10; i++) {
                runAll(main, Setup.class);
                runAll(main, Benchmark.class);
                runAll(main, TearDown.class);
            }

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
        final long accountId = 12;
        final long requestId = 76;
        final long concertId;
        final short numSeats;
        final long sectionId;
        switch (counter++ & 3) {
            case 0:
                concertId = 1;
                numSeats = 1;
                sectionId = 1;
                break;
            case 1:
                concertId = 2;
                numSeats = 8;
                sectionId = 2;
                break;
            case 2:
                concertId = 1;
                numSeats = 3;
                sectionId = 3;
                break;
            case 3:
                concertId = 2;
                numSeats = 20;
                sectionId = 5;
                break;
            default:
                throw new IllegalStateException();
        }

        TicketPurchase ticketPurchase = newTicketPurchase(concertId, sectionId, numSeats, accountId, requestId);
        commandHandlerProxy.onTicketPurchase(ticketPurchase);

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

        commandHandlerProxy = commandHandlerQueue.createAppender().methodWriter(CommandHandler.class);

        ConcertFactory.createConcerts().stream().forEachOrdered(commandHandlerProxy::onCreateConcert);
        assertTrue(commandHandlerReader.readOne());
        assertTrue(eventHandlerReader.readOne());
        assertTrue(commandHandlerReader.readOne());
        assertTrue(eventHandlerReader.readOne());

    }
}
