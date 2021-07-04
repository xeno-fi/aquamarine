package fi.xeno.aquamarine.util;

public class TimestampedValue<T> {
    
    private long timestamp;
    private T value;

    public TimestampedValue(long timestamp, T value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
    
    public void update() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public boolean isOlderThan(long millis) {
        return System.currentTimeMillis() - this.timestamp >= millis;
    }
    
}
