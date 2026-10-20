# Quarkus Messaging Nats Jetstream

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.reactivemessaging.nats-jetstream/quarkus-messaging-nats-jetstream?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.reactivemessaging.nats-jetstream/quarkus-messaging-nats-jetstream)

## Introduction
This is a Quarkus extension that integrates [NATS JetStream](https://docs.nats.io/nats-concepts/jetstream) with [SmallRye Reactive Messaging](https://smallrye.io/smallrye-reactive-messaging), allowing Quarkus applications to use NATS JetStream for message streaming in both JVM and Native modes. The connector name is `quarkus-jetstream`.

For more information about installation and configuration, please read the documentation
[here](https://docs.quarkiverse.io/quarkus-reactive-messaging-nats-jetstream/dev/).

## Contributing
Feel free to contribute to this project by submitting issues or pull requests.

## Build Commands

### Full Build
```bash
./mvnw clean install
```

### Build without formatting
```bash
./mvnw clean install -Dno-format
```

### Native Build
```bash
./mvnw install -Dnative -Dquarkus.native.container-build -Dnative.surefire.skip
```

### Run Tests Only
```bash
./mvnw test
```

### Run Integration Tests
```bash
./mvnw verify -Pit
```

### Run Single Test
```bash
# For client extension tests
./mvnw test -pl client/runtime -Dtest=ClassName

# For messaging extension tests
./mvnw test -pl messaging/deployment -Dtest=ClassName

# For integration tests
./mvnw test -pl integration-tests -Dtest=ClassName
```

### Build Documentation
```bash
./mvnw install -Pdocs
```

## Module Structure

This is a multi-module Maven project containing two Quarkus extensions:

- **client/**: The NATS client extension (`quarkus-nats-client`)
  - **client/runtime/**: Reactive NATS/JetStream client, datasource configuration (`quarkus.messaging.nats.*`), `Client` CDI beans, TLS, tracing and resource (stream/consumer/KV/object store) initialization
  - **client/deployment/**: Build steps registering the `Client` beans, serializer, tracing, native image support and Dev Services
- **messaging/**: The SmallRye Reactive Messaging connector extension (`quarkus-messaging-nats-jetstream`), built on the client extension
  - **messaging/runtime/**: The `quarkus-jetstream` connector, message processors, channel configuration and request/reply
  - **messaging/deployment/**: Build steps registering the connector and per-channel processor beans
- **test/**: Shared NATS Testcontainers support used by Dev Services and tests
- **integration-tests/**: End-to-end tests using the extension
- **docs/**: Documentation in AsciiDoc format

## Architecture

### Core Components

**JetStreamConnector** (`messaging/runtime/src/main/java/.../JetStreamConnector.java`):
- Main entry point implementing both `InboundConnector` and `OutboundConnector`
- Creates publishers for incoming channels and subscribers for outgoing channels
- Manages health reporting for all message processors
- Uses factory pattern to create processor instances

**Message Processors**:
- **MessagePublisherProcessor**: Handles incoming messages from NATS JetStream (pull or push consumers)
- **MessageSubscriberProcessor**: Handles outgoing messages to NATS JetStream
- Located in `messaging/runtime/src/main/java/.../processors/`
- Each processor tracks its own health status

**Request/Reply** (`messaging/runtime/src/main/java/.../reply/`):
- **JetStreamRequestReply**: request/reply emitter injectable via `@Channel`; publishes on the channel subject and awaits a correlation-matched reply on a shared reply subject (non-durable consumer per instance, `deliver_policy=new`)
- **JetStreamRequestReplyFactory** / **JetStreamRequestReplyProducer**: SmallRye `EmitterFactory` SPI + CDI producer that make the type injectable; emitters are closed on shutdown
- **CorrelationIdHandler** / **ReplyFailureHandler**: pluggable correlation-id and failure strategies, selected per channel via `reply.correlation-id.handler` / `reply.failure.handler`
- See the user documentation (Request/Reply section in `docs/modules/ROOT/pages/index.adoc`) for examples and the `reply.*` configuration reference

**Client Layer** (`client/runtime/src/main/java/.../client/`):
- **connection/**: Manages NATS connections and configuration
- **context/**: Handles JetStream context creation and management
- **consumer/**: Consumer implementations (pull and push)
- **publisher/**: Message publishing logic
- **stream/**: Stream management and configuration
- **store/**: Key/Value store support
- **tracing/**: OpenTelemetry integration for distributed tracing

**Configuration** (`client/runtime/src/main/java/.../client/runtime/configuration/` for datasources, `messaging/runtime/src/main/java/.../connector/configuration/` for channels):
- **mapper/**: Maps between Quarkus configuration and NATS JetStream API objects
- Handles both stream and consumer configuration (pull and push)
- Supports Key/Value store configuration

### Message Flow

**Incoming (Pull/Push Consumer → Application)**:
1. `JetStreamConnector.getPublisher()` called by SmallRye Reactive Messaging
2. Factory creates appropriate `MessagePublisherProcessor`
3. Processor subscribes to NATS JetStream consumer
4. Messages flow to application via Reactive Streams `Publisher`

**Outgoing (Application → JetStream Stream)**:
1. `JetStreamConnector.getSubscriber()` called by SmallRye Reactive Messaging
2. Factory creates `MessageSubscriberProcessor`
3. Application publishes messages via Reactive Streams `Subscriber`
4. Processor publishes to specified NATS JetStream subject/stream

### Configuration Hierarchy

1. **Connection-level**: `quarkus.messaging.nats.connection.*` - Defines NATS server connection
2. **Stream-level**: `quarkus.messaging.nats.streams.[stream-name].*` - Defines JetStream streams
3. **Consumer-level**: `quarkus.messaging.nats.streams.[stream-name].pull-consumers.[consumer-name].*` or `push-consumers.[consumer-name].*`
4. **Channel-level**: `mp.messaging.incoming.[channel].*` or `mp.messaging.outgoing.[channel].*` - Maps channels to streams/consumers/subjects

## Key Development Notes

### Testing Strategy

- **client/runtime/src/test/**: Client tests against a NATS Testcontainer
- **messaging/deployment/src/test/**: Unit tests that start a Quarkus application with the extension
- **integration-tests/src/test/**: Integration tests using the full extension (JVM and Native)
- Tests use Quarkus Dev Services to automatically start a NATS container via Testcontainers
- Common test utilities in `messaging/deployment/src/test/java/.../test/`

### Dev Services

The extension provides Dev Services support that automatically starts a NATS server container during development and testing. Configuration in `client/deployment/src/main/java/.../client/deployment/JetStreamDevServicesProcessor.java`.

### GraalVM Native Support

Native image configuration for the NATS Java client (reflection, runtime-initialized classes, SSL) is handled by `NatsClientProcessor` in `client/deployment/`.

### Annotation Processors

The build uses several annotation processors:
- SmallRye Connector Attribute Processor: Generates connector configuration publishMetadata
- Lombok: For code generation
- Quarkus Extension Processor: Generates extension publishMetadata

### Channel Configuration Requirements

**Incoming channels** require:
- `stream`: The JetStream stream name
- `consumer`: The consumer name (must be pre-configured or auto-configured)

**Outgoing channels** require:
- `stream`: The JetStream stream name
- `subject`: The subject to publish to

### Health Checks

The connector implements `HealthReporter` and reports both liveness and readiness based on the health status of all active message processors (connection state, consumer state, etc.).

## Technology Stack

- **Quarkus**
- **Java**: 21 (minimum)
- **NATS Java Client**
- **SmallRye Reactive Messaging**: Provided by Quarkus BOM
- **OpenTelemetry**: For distributed tracing
- **Lombok**: For reducing boilerplate
- **Jackson**: For JSON serialization/deserialization
