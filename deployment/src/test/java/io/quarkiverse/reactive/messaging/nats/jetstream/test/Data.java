package io.quarkiverse.reactive.messaging.nats.jetstream.test;

public class Data {
    private String data;
    private String resourceId;
    private String messageId;

    public Data(String data, String resourceId, String messageId) {
        this.data = data;
        this.resourceId = resourceId;
        this.messageId = messageId;
    }

    public Data() {
    }

    public String getData() {
        return data;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getMessageId() {
        return messageId;
    }
}
