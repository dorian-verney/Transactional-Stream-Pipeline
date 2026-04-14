package consumers.applications;

import ingestion.wrapper.Transaction;
import utils.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

public class PnLCalculator {

    private static final Logger logger = LoggerFactory.create(PnLCalculator.class, "PnL.log");

    // positionId → leg BUY en attente
    private static final Map<Long, Transaction> openPositions = new ConcurrentHashMap<>();

    // positionId → P&L final du round-trip
    private static final Map<Long, Double> closedPnL = new ConcurrentHashMap<>();

    private static final Map<Long, Transaction> pendingSells = new ConcurrentHashMap<>();

    // -------------------------------------------------------

    public static void process(Transaction tx) {
        switch (tx.getSide()) {
            case "BUY"  -> openPosition(tx);
            case "SELL" -> closePosition(tx);
            default     -> System.err.println("Unknown Side : " + tx.getSide());
        }
    }

    // -------------------------------------------------------

    private static void openPosition(Transaction buy) {
        retryPending();

        openPositions.put(buy.getId(), buy);
//        System.out.printf("[OPEN]  positionId=%-8s | %s %.2f @ %.4f%n",
//                buy.positionId(), buy.currency(), buy.amount(), buy.executionPrice());
    }

    // -------------------------------------------------------

    private static void closePosition(Transaction sell) {
        Transaction buy = openPositions.remove(sell.getPositionId());
        if (buy == null) {
            pendingSells.put(sell.getPositionId(), sell);
            logger.log(Level.INFO, "Pas de BUY trouvé pour positionId=" + sell.getPositionId());
            return;
        }

        double pnl = computeRoundTrip(buy, sell);
        closedPnL.put(sell.getPositionId(), pnl);

//        System.out.printf("[CLOSE] positionId=%-8s | P&L = %+.2f USD  (%s)%n",
//                sell.getPositionId(), pnl, pnl >= 0 ? "GAIN" : "LOSS");
    }

    // -------------------------------------------------------

    // calling periodically
    public static void retryPending() {
        pendingSells.forEach((posId, sell) -> {
            Transaction buy = openPositions.remove(posId);
            if (buy != null) {
                logger.log(Level.INFO, "Position ID removed pending = "+posId);
                pendingSells.remove(posId);
                double pnl = computeRoundTrip(buy, sell);
                closedPnL.put(posId, pnl);
            }
        });
    }

    // -------------------------------------------------------

    /**
     * Round-trip P&L :
     *   revenue  = amount * executionPrice_sell
     *   cost     = amount * executionPrice_buy
     *   fees     = (fee_buy + fee_sell) * exchangeRate moyen
     *   P&L      = revenue - cost - fees
     *
     * Tout est converti en USD via executionPrice.
     */
    private static double computeRoundTrip(Transaction buy, Transaction sell) {
        double revenue  = sell.getAmount() * sell.getExchangeRate();
        double cost     = buy.getAmount()  * buy.getExchangeRate();
        double avgRate  = (buy.getExchangeRate() + sell.getExchangeRate()) / 2.0;
        double totalFee = (buy.getFee() + sell.getFee()) * avgRate;

        return revenue - cost - totalFee;
    }

    // -------------------------------------------------------


    public static double getTotalPnL() {
        return closedPnL.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}