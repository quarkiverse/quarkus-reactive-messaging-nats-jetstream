package io.quarkiverse.reactive.messsaging.nats.jetstream.setup;

import java.util.Set;

public class Stream {
    private final String name;
    private final Set<String> subjects;

    public Stream(final String name, final Set<String> subjects) {
        this.name = name;
        this.subjects = subjects;
    }

    public String name() {
        return name;
    }

    public Set<String> subjects() {
        return subjects;
    }

    @Override
    public String toString() {
        return "Stream{" +
                "name='" + name + '\'' +
                ", subjects=" + subjects +
                '}';
    }
}
