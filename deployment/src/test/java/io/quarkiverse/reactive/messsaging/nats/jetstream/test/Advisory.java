package io.quarkiverse.reactive.messsaging.nats.jetstream.test;

public class Advisory {
    private String type;
    private String id;
    private String timestamp;
    private String stream;
    private String consumer;
    private long stream_seq;
    private long deliveries;

    public Advisory() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public long getStream_seq() {
        return stream_seq;
    }

    public void setStream_seq(long stream_seq) {
        this.stream_seq = stream_seq;
    }

    public long getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(long deliveries) {
        this.deliveries = deliveries;
    }
}
