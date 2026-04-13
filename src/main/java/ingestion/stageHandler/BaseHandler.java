package ingestion.stageHandler;

import ingestion.wrapper.DataSource;
import ingestion.wrapper.Wrapper;

public abstract class BaseHandler<I, O> implements StageHandler<I, O> {
    @Override
    public abstract Wrapper<O> execute(Wrapper<I> input);
}
