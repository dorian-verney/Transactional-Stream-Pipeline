package ingestion.wrapper;

public class QueueRequest
{
    private long id;
    private Transaction tx;

    public static final QueueRequest EOF = new QueueRequest(-1, null);

    public QueueRequest(long id, Transaction tx){
        this.id = id;
        this.tx = tx;
    }

    public Transaction getTx(){
        return tx;
    }

    public boolean isEOF(){
        return this == EOF;
    }

    public String toString(){
        if (isEOF()) return "EOF";
        return "Req="+ id + " ("+tx.toString()+")";
    }
}
