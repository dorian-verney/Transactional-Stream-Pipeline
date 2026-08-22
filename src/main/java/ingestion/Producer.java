package ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import components.MetricComputer;
import components.MonitoredQueue;
import ingestion.stageHandler.*;
import ingestion.wrapper.QueueRequest;
import ingestion.wrapper.Transaction;
import ingestion.wrapper.Wrapper;


import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class Producer implements Runnable {

    private final MonitoredQueue<String> rawQueue;
    private final MonitoredQueue<QueueRequest> processedQueue;

    private final int numConsumers;

    private final MetricComputer<String> metricComputer;


    public Producer(MonitoredQueue<String> rawQueue,
                    MonitoredQueue<QueueRequest> processedQueue,
                    int numConsumers,
                    MetricComputer<String> metricComputer)
    {
        this.rawQueue = rawQueue;
        this.processedQueue = processedQueue;
        this.numConsumers = numConsumers;
        this.metricComputer = metricComputer;
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
        int[] depth = {0};
        metricComputer.startAnalyzing();
        try {
            while (true) {

                // TAKE from RAW QUEUE
                String jsonRec = rawQueue.take();

                // PUT to PROCESSED QUEUE
                if (isValidRecord(jsonRec, buffer, depth)) {
                    Wrapper<QueueRequest> req = runChain(new Wrapper<>(buffer.toString()));

                    if (!req.isFailed()) {
//                        metricsLog.logIf(processedQueue.isFull(1),
//                                Level.WARNING, "PROCESSED QUEUE full");

                        while (!processedQueue.offer(req.getPayload())) {
                            Thread.sleep(1);
                        }
                    }
                    buffer.setLength(0);
                }
                metricComputer.updateMetric();
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
