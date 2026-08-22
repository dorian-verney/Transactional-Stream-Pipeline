package consumers;

import components.MetricComputer;
import components.MonitoredQueue;
import consumers.applications.PnLCalculator;
import ingestion.wrapper.QueueRequest;
import java.util.concurrent.CountDownLatch;


public class Consumer implements Runnable {

    private final MonitoredQueue<QueueRequest> queue;
    private final CountDownLatch consumersDone;
    private final MetricComputer<QueueRequest> metricComputer;

    public Consumer(MonitoredQueue<QueueRequest> queue,
                    CountDownLatch consumersDone,
                    MetricComputer<QueueRequest> metricComputer)
    {
        this.queue = queue;
        this.consumersDone = consumersDone;
        this.metricComputer = metricComputer;
    }

    @Override
    public void run() {
        try {
            while (true)
            {
                // TAKE from PROCESSED QUEUE
                QueueRequest req = queue.take();
                if (req.isEOF()) break;

                // Simulate a CPU work in ms
                PnLCalculator.process(req.getTx());
                Thread.sleep(5);

                // update metric
                metricComputer.updateMetric();

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            consumersDone.countDown();
        }
        System.out.println("["+ Thread.currentThread().getName() + "] done.");
    }
}
