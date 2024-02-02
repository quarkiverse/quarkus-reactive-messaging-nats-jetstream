package io.quarkiverse.reactive.messaging.nats.jetstream.it;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Data {
    private String data;
    private String resourceId;

    private String messageId;

    public Data(String data, String resourceId) {
        this(data, resourceId, null);
    }

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

    public void setData(String data) {
        this.data = data;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
