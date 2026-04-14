package consumers;

import ingestion.Producer;
import ingestion.wrapper.QueueRequest;
import utils.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Logger;

public class Consumers {

    private static final Logger logger = LoggerFactory.create(Consumers.class, "Consumers.log");

    private final int numConsumers;
    private List<Consumer> consumers;
    private final BlockingQueue<QueueRequest> processedQueue;

    CountDownLatch consumersDone;
    LongAdder adderReq = new LongAdder();

    public Consumers(BlockingQueue<QueueRequest> queue, int numConsumers){
        this.processedQueue = queue;
        this.numConsumers = numConsumers;
        consumersDone = new CountDownLatch(numConsumers);
    }

    public void start(){
        for (int i=0; i<numConsumers; i++)
        {
            int id = i;
            Thread.ofVirtual().name("Consumer-" + id).start(
                    new Consumer(logger, processedQueue, consumersDone, adderReq)
            );
        }
    }

    public CountDownLatch getLatch(){return consumersDone;}

}
