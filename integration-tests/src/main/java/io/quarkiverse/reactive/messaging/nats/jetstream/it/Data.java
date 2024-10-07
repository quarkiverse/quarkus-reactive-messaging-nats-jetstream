package io.quarkiverse.reactive.messaging.nats.jetstream.it;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonView;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Data {

    private String data;
    private String resourceId;

    private String messageId;

    @JsonView(IncludeTimestamps.class)
    private Instant creationTime;

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

    public Data(String data, String resourceId, String messageId, Instant creationTime) {
        this.data = data;
        this.resourceId = resourceId;
        this.messageId = messageId;
        this.creationTime = creationTime;
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

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }
}
