package dataSource;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class TransactionSource implements Runnable {

    private final BlockingQueue<String> rawStream;
    private final int normalRate;   // tx/sec en régime normal
    private int burstRate;    // tx/sec pendant un burst
    private int burstEveryMs; // fréquence des bursts
    private int burstDurationMs;
    private volatile boolean running = true;

    public TransactionSource(BlockingQueue<String> rawStream, int ratePerSecond) {
        this.rawStream = rawStream;
        this.normalRate = ratePerSecond;
    }

    public TransactionSource(BlockingQueue<String> rawStream, int normalRate,
                             int burstRate, int burstEveryMs, int burstDurationMs) {
        this.rawStream = rawStream;
        this.normalRate = normalRate;
        this.burstRate  = burstRate;
        this.burstEveryMs = burstEveryMs;
        this.burstDurationMs = burstDurationMs;
    }

    @Override
    public void run() {
        generateWithoutBurst();
    }

    private void generateWithoutBurst(){
        long intervalNanos = 1_000_000_000L / normalRate;
        long id = 0;

        while (running) {
            long start = System.nanoTime();

            String json = generateTransaction(id++);
            // offer() non-bloquant : si la queue est pleine, on DROP (load shedding natif)
            // PUT to PROCESSED QUEUE
            boolean accepted = rawStream.offer(json);

            // busy-wait précis pour tenir le rate
            long elapsed = System.nanoTime() - start;
            long toWait = intervalNanos - elapsed;
            if (toWait > 0) {
                long deadline = System.nanoTime() + toWait;
                while (System.nanoTime() < deadline) Thread.onSpinWait();
            }
        }
    }

    private void generateWithBurst(){
        long lastBurst = System.currentTimeMillis();
        boolean inBurst = false;
        long burstEnd = 0;
        long id = 0;

        while (running) {
            long now = System.currentTimeMillis();

            // Déclenchement burst
            if (!inBurst && now - lastBurst >= burstEveryMs) {
                inBurst = true;
                burstEnd = now + burstDurationMs;
                lastBurst = now;
                System.out.println("[SOURCE] Burst triggered!");
            }
            if (inBurst && now >= burstEnd) {
                inBurst = false;
            }

            int currentRate = inBurst ? burstRate : normalRate;
            long intervalNanos = 1_000_000_000L / currentRate;

            rawStream.offer(generateTransaction(id++));

            long deadline = System.nanoTime() + intervalNanos;
            while (System.nanoTime() < deadline) Thread.onSpinWait();
        }
    }

    public void stop() { running = false; }

    private String generateTransaction(long id) {
        String[] currencies = {"EUR", "USD", "CHF", "GBP", "JPY"};
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        return String.format(
                "{\"id\":%d,\"currency\":\"%s\",\"date\":\"%s\",\"amount\":%.2f,\"fee\":%.2f,\"exchangeRate\":%.4f}",
                id,
                currencies[rng.nextInt(currencies.length)],
                Instant.now().toString(),
                rng.nextDouble(100, 10_000),
                rng.nextDouble(1, 50),
                rng.nextDouble(0.8, 1.5)
        );
    }
}