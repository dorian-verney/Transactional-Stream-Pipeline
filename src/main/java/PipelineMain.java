import consumers.Consumers;
import ingestion.Producer;
import ingestion.wrapper.QueueRequest;
import consumers.Consumer;
import dataSource.TransactionSource;
import utils.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PipelineMain
{
    private static final Logger logger = Logger.getLogger(PipelineMain.class.getName());

    private static final int RAW_QUEUE_CAPACITY = 100;
    private static final int PROCESSED_QUEUE_CAPACITY = 2000;
    private static final int NUM_CONSUMERS  = 6;

    public static void main(String[] args) throws InterruptedException {

        List.of("Producer.log", "Consumers.log").forEach(filename -> {
            try {
                Files.deleteIfExists(Path.of(System.getProperty("user.dir") + "/monitoring/" + filename));
            } catch (IOException e) {
                System.err.println("Failed to delete " + filename + ": " + e.getMessage());
            }
        });

        BlockingQueue<String> rawQueue = new LinkedBlockingQueue<>(RAW_QUEUE_CAPACITY);
        BlockingQueue<QueueRequest> processedQueue = new LinkedBlockingQueue<>(PROCESSED_QUEUE_CAPACITY);
        AtomicBoolean running = new AtomicBoolean(true);

        // Clean Stopping Hook (^C or system signal)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
           System.err.println("Application Stopped !");
            running.set(false);
        }));

        logger.info( "Application Started !");

        Thread sourceThread = new Thread(new TransactionSource(rawQueue, 5000));
        sourceThread.start();

        Thread producerThread = new Thread(new Producer(rawQueue, processedQueue, PROCESSED_QUEUE_CAPACITY, NUM_CONSUMERS));
        producerThread.start();

        Consumers consumers = new Consumers(processedQueue, NUM_CONSUMERS);
        consumers.start();


        producerThread.join();
        consumers.getLatch().await();
        logger.info("Application Done !");
    }

}
