package consumers;

import components.MetricComputer;
import components.MonitoredQueue;
import ingestion.wrapper.QueueRequest;
import java.util.concurrent.CountDownLatch;

public class Consumers {

    private final MetricComputer<QueueRequest> metricComputer;

    private final int numConsumers;
    private final MonitoredQueue<QueueRequest> processedQueue;

    CountDownLatch consumersDone;


    public Consumers(MonitoredQueue<QueueRequest> queue,
                     int numConsumers,
                     MetricComputer<QueueRequest> metricComputer)
    {
        this.processedQueue = queue;
        this.numConsumers = numConsumers;
        this.consumersDone = new CountDownLatch(numConsumers);
        this.metricComputer = metricComputer;
    }

    public void start(){
        metricComputer.startAnalyzing();
        for (int i=0; i<numConsumers; i++)
        {
            int id = i;
            Thread.ofVirtual().name("Consumer-" + id).start(
                    new Consumer(processedQueue, consumersDone, metricComputer)
            );
        }
    }

    public CountDownLatch getLatch(){return consumersDone;}

}
