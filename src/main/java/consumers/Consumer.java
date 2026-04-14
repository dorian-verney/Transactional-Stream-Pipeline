package consumers;

import consumers.applications.PnLCalculator;
import ingestion.Producer;
import ingestion.wrapper.QueueRequest;
import utils.LoggerFactory;
import utils.MetricsLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer implements Runnable
{
    private final MetricsLogger metricsLog;

    private final BlockingQueue<QueueRequest> queue;
    private final CountDownLatch consumersDone;
    private final LongAdder adder;

    public Consumer(Logger logger,
                    BlockingQueue<QueueRequest> queue,
                    CountDownLatch consumersDone,
                    LongAdder adder)
    {
        this.queue = queue;
        this.consumersDone = consumersDone;
        this.adder = adder;
        this.metricsLog = new MetricsLogger(logger);
    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            while (true)
            {
                // TAKE from RAW QUEUE
                QueueRequest req = queue.take();
                adder.increment();
                if (req.isEOF()) break;

                // Simulate a CPU work in ms
                PnLCalculator.process(req.getTx());
                Thread.sleep(5);

                metricsLog.logEvery(adder.longValue(), 100, () ->
                        String.format("CONS rate=%.2f req/s", (double) adder.longValue()/(System.currentTimeMillis() - start) * 1000)
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            consumersDone.countDown();
        }
        System.out.println("["+ Thread.currentThread().getName() + "] done.");
    }
}
