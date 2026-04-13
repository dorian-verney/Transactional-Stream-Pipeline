package ingestion.stageHandler;

import ingestion.wrapper.Wrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public class JsonRecordStage extends BaseHandler<String, JsonNode> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Wrapper<JsonNode> execute(Wrapper<String> input) {
        if (input.isFailed()){
            return Wrapper.failed();
        }
        try {
            String rawLine = input.getPayload();
            JsonNode node = MAPPER.readTree(rawLine);
            return Wrapper.success(node);
        } catch (Exception e) {
            return Wrapper.failed(e.getMessage());
        }
    }

}
