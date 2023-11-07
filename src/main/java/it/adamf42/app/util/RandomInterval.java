package it.adamf42.app.util;

public class RandomInterval implements TimeInterval {


    private final long minSeconds;
    private final long maxSeconds;

    public RandomInterval(int minSeconds, int maxSeconds) {
        this.minSeconds = minSeconds;
        this.maxSeconds = maxSeconds;
    }


    @Override
    public long getInterval() {
        return (int) Math.floor(Math.random() * (maxSeconds - minSeconds + 1) + minSeconds);
    }
}
