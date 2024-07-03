package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Data data1 = (Data) o;
        return Objects.equals(data, data1.data) && Objects.equals(resourceId, data1.resourceId)
                && Objects.equals(messageId, data1.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, resourceId, messageId);
    }
}
