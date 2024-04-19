package io.quarkiverse.reactive.messaging.nats.jetstream.test;

public class SubjectData {
    private String data;
    private String resourceId;
    private String messageId;
    private String subject;

    public SubjectData(String data, String resourceId, String messageId, String subject) {
        this.data = data;
        this.resourceId = resourceId;
        this.messageId = messageId;
        this.subject = subject;
    }

    public SubjectData() {
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

    public String getSubject() {
        return subject;
    }
}
