package ingestion.stageHandler;

import com.fasterxml.jackson.databind.JsonNode;
import ingestion.wrapper.*;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class TransactionStage extends BaseHandler<JsonNode, Transaction>{

    private static final String[] FIELDS= {"id", "currency", "date", "amount", "fee", "exchangeRate"};

    @Override
    public Wrapper<Transaction> execute(Wrapper<JsonNode> input) {
        if (input.isFailed()){
            return Wrapper.failed();
        }
        try {
            JsonNode node = input.getPayload();
            Transaction tx = validAndAssign(node);
            return Wrapper.success(tx);
        } catch (Exception e){
            return Wrapper.failed(e.getMessage());
        }
    }

    public Transaction validAndAssign(JsonNode node){
        try {
            // missing fields
            for (String field : FIELDS)
            {
                if (!node.hasNonNull(field))
                    return null;
            }

            Instant date;
            try {
                date = Instant.parse(node.get("date").asText());
            } catch (DateTimeParseException e) {
                return null;
            }

            return new Transaction(
                    node.get("id").asLong(),
                    node.get("currency").asText(),
                    date,
                    node.get("amount").asDouble(),
                    (float) node.get("fee").asDouble(),
                    (float) node.get("exchangeRate").asDouble()
                );

        } catch (Exception e) {
            IO.println("ERROR parsing JSON -> TRANSACTION " + e.getMessage());
            return null;
        }
    }
}
