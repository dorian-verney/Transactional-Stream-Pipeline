package ingestion.wrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ingestion.stageHandler.TransactionStage;
import utils.LoggerUtil;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class Transaction
{
    private long    id;
    private String  currency;
    private Instant date;
    private double  amount;
    private float   fee;
    private float   exchangeRate;

    public Transaction(long id, String currency, Instant date,
                       double amount, float fee, float exchangeRate){
        this.id = id;
        this.currency = currency;
        this.date = date;
        this.amount = amount;
        this.fee = fee;
        this.exchangeRate = exchangeRate;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", currency='" + currency + '\'' +
                ", date=" + date +
                ", amount=" + amount +
                ", fee=" + fee +
                ", exchangeRate=" + exchangeRate +
                '}';
    }

    public long getId() {
        return id;
    }

}
