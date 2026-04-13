package ingestion.wrapper;

public abstract class BaseWrapperDataSource<I, O> implements DataSource<I, O>{

    protected DataSource wrapped;
    private I payload;
    public abstract O execute(I input);

}
