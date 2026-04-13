package ingestion.wrapper;

public interface DataSource<I, O>
{
    Wrapper<O> execute(Wrapper<I> input);
}
