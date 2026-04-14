package ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import consumers.applications.PnLCalculator;
import ingestion.stageHandler.*;
import ingestion.wrapper.QueueRequest;
import ingestion.wrapper.Transaction;
import ingestion.wrapper.Wrapper;
import utils.LoggerFactory;
import utils.MetricsLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.*;

public class Producer implements Runnable
{
    private static final Logger logger = LoggerFactory.create(Producer.class, "Producer.log");
    private static final MetricsLogger metricsLog = new MetricsLogger(logger);

    private final BlockingQueue<String> rawQueue;
    private final BlockingQueue<QueueRequest> processedQueue;

    private final int processedQueueCapacity;

    private final int numConsumers;

    public Producer(BlockingQueue<String> rawQueue,
                    BlockingQueue<QueueRequest> processedQueue,
                    int processedQueueCapacity,
                    int numConsumers)
    {
        this.rawQueue = rawQueue;
        this.processedQueue = processedQueue;
        this.processedQueueCapacity = processedQueueCapacity;
        this.numConsumers = numConsumers;
    }

    public Wrapper<QueueRequest> runChain(Wrapper<String> rawLine) {
        Wrapper<JsonNode>   json = new JsonRecordStage().execute(rawLine);
        Wrapper<Transaction> tx  = new TransactionStage().execute(json);
        return new QueueRequestStage().execute(tx);
    }

    @Override
    public void run()
    {
        StringBuilder buffer = new StringBuilder();
        String line = "";
        int[] depth = {0};
        int nbReq = 0;
        try {
            long start = System.currentTimeMillis();
            while (true) {

                // TAKE from RAW QUEUE
                String jsonRec = rawQueue.take();

                // PUT to PROCESSED QUEUE
                if (isValidRecord(jsonRec, buffer, depth)) {
                    Wrapper<QueueRequest> req = runChain(new Wrapper<>(buffer.toString()));

                    if (!req.isFailed()) {
                        metricsLog.logIf(processedQueue.size() == processedQueueCapacity,
                                Level.WARNING, "PROCESSED QUEUE full");

                        while (!processedQueue.offer(req.getPayload())) {
                            Thread.sleep(1);
                        }
                    }
                    buffer.setLength(0);
                }

                nbReq++;
                final double rate = (double) nbReq / (System.currentTimeMillis() - start);
                metricsLog.logEvery(nbReq, 100, () ->
                        String.format("PROD rate=%.2f req/s", rate * 1000)
                );
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            sendEOF();
        }
    }


    private boolean isValidRecord(String line, StringBuilder buffer, int[] depth){
        // Filter
        if (line.isBlank() ) {
            return false;
        }
        line = line.trim();

        if (line.equals("[") || line.equals("]")) return false;
        if (line.startsWith("{")) depth[0]++;
        buffer.append(line);
        if (line.endsWith("}") | line.endsWith("},")) depth[0]--;

        return depth[0] == 0 && !buffer.isEmpty();
    }

    private void sendEOF(){
        for (int i=0; i<numConsumers; i++) {
            try {
               boolean sent = processedQueue.offer(QueueRequest.EOF, 5, TimeUnit.SECONDS);
                if (!sent)
                    System.err.println("[Producer] Timeout sending OEF #" + i);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
