package consumers;

import ingestion.wrapper.QueueRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;

public class Consumer implements Runnable
{
    private final BlockingQueue<QueueRequest> queue;
    private final CountDownLatch consumersDone;
    private LongAdder adder;

    public Consumer(BlockingQueue<QueueRequest> queue,
                    CountDownLatch consumersDone,
                    LongAdder adder)
    {
        this.queue = queue;
        this.consumersDone = consumersDone;
        this.adder = adder;
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
                Thread.sleep(20);


                if (adder.longValue() % 100 == 0){
                    double elapseMilli = System.currentTimeMillis() - start;
                    System.out.format("CONS rate=%.2f req/s \n", (adder.longValue()/elapseMilli)*1000);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            consumersDone.countDown();
        }
        System.out.println("["+ Thread.currentThread().getName() + "] done.");
    }
}
