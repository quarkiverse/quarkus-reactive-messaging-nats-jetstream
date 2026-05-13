package io.quarkiverse.reactive.nats;

import java.util.List;

public interface ServerInfo {

    String serverId();
    String serverName();
    String version();
    String goVersion();
    String host();
    int port();
    boolean headersSupported();
    boolean authRequired();
    private final boolean tlsRequired;
    private final boolean tlsAvailable;
    private final long maxPayload;
    private final List<String> connectURLs;
    private final int protocolVersion;
    private final byte[] nonce;
    private final boolean lameDuckMode;
    private final boolean jetStream;
    private final int clientId;
    private final String clientIp;
    private final String cluster;

}
