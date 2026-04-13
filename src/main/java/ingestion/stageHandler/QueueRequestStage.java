package ingestion.stageHandler;

import com.fasterxml.jackson.databind.JsonNode;
import ingestion.wrapper.QueueRequest;
import ingestion.wrapper.Transaction;
import ingestion.wrapper.Wrapper;

public class QueueRequestStage extends BaseHandler<Transaction, QueueRequest> {


    @Override
    public Wrapper<QueueRequest> execute(Wrapper<Transaction> input) {
        if (input.isFailed()){
            return Wrapper.failed();
        }
        try {
            Transaction tx = input.getPayload();
            QueueRequest req = new QueueRequest(tx.getId(), tx);
            return new Wrapper<>(req);
        } catch (Exception e) {
            return Wrapper.failed(e.getMessage());
        }
    }
}
