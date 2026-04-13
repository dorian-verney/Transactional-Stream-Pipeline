package ingestion.stageHandler;

import ingestion.wrapper.DataSource;
import ingestion.wrapper.Wrapper;

public interface StageHandler<I, O> {

    Wrapper<O> execute(Wrapper<I> input);
}
