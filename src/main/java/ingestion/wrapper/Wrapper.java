package ingestion.wrapper;

import ingestion.stageHandler.IngestionStatus;

public class Wrapper<I> {
    private I payload;
    private final long timestamp;
    private IngestionStatus status;
    private String errorMessage = "";

    public Wrapper(){
        timestamp = System.currentTimeMillis();
    }

    public Wrapper(I input){
        payload = input;
        timestamp = System.currentTimeMillis();
    }

    public I getPayload(){
        return payload;
    }

    public long timestamp(){
        return timestamp;
    }

    public boolean isFailed(){
        return status == IngestionStatus.FAILED || status == IngestionStatus.SKIPPED;
    }

    public static <I>Wrapper<I> failed(){
        Wrapper<I> w = new Wrapper<>();
        w.status = IngestionStatus.SKIPPED;
        return w;
    }

    public static <I>Wrapper<I> failed(String errorMessage){
        Wrapper<I> w = new Wrapper<>();
        w.status = IngestionStatus.FAILED;
        w.errorMessage = errorMessage;
        return w;
    }

    public static <I>Wrapper<I> success(I input){
        Wrapper<I> w = new Wrapper<>(input);
        w.status = IngestionStatus.SUCCESS;
        return w;
    }

}
