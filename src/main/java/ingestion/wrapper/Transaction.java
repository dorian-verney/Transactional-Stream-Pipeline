package ingestion.wrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ingestion.stageHandler.TransactionStage;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class Transaction
{
    private final long    id;
    private final long    positionId;
    private final String  side;
    private final String  currency;
    private final Instant date;
    private final double  amount;
    private final float   fee;
    private final float   exchangeRate;

    public Transaction(long id, long positionId, String side, String currency, Instant date,
                       double amount, float fee, float exchangeRate){
        this.id = id;
        this.positionId = positionId;
        this.side = side;
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
                ", positionId=" + positionId +
                ", side='" + side + '\'' +
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

    public String getSide() {
        return side;
    }

    public long getPositionId() {
        return positionId;
    }

    public float getExchangeRate() {
        return exchangeRate;
    }

    public float getFee() {
        return fee;
    }

    public double getAmount() {
        return amount;
    }
}
