import ingestion.Producer;
import ingestion.wrapper.QueueRequest;
import consumers.Consumer;
import source.TransactionSource;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

public class PipelineMain
{

    private static final int RAW_QUEUE_CAPACITY = 100;

    private static final int PROCESSED_QUEUE_CAPACITY = 1000;
    private static final int NUM_CONSUMERS  = 3;

    public static void main(String[] args) throws InterruptedException {

        BlockingQueue<String> rawQueue = new LinkedBlockingQueue<>(RAW_QUEUE_CAPACITY);
        BlockingQueue<QueueRequest> processedQueue = new LinkedBlockingQueue<>(PROCESSED_QUEUE_CAPACITY);
        AtomicBoolean running = new AtomicBoolean(true);
        CountDownLatch consumersDone = new CountDownLatch(NUM_CONSUMERS);

        // Clean Stopping Hook (^C or system signal)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Main] STOP..");
            running.set(false);
        }));


        Thread sourceThread = new Thread(new TransactionSource(rawQueue, 1000));
        sourceThread.start();

        Thread producerThread = new Thread(new Producer(rawQueue, processedQueue, running, NUM_CONSUMERS));
        producerThread.start();

        LongAdder adderReq = new LongAdder();

        for (int i=0; i<NUM_CONSUMERS; i++)
        {
            int id = i;
            Thread.ofVirtual().name("Consumer-" + id).start(
                    new Consumer(processedQueue, consumersDone, adderReq)
            );
        }

        producerThread.join();
        consumersDone.await();
        IO.println("[Main] Pipeline done.");
    }


}
