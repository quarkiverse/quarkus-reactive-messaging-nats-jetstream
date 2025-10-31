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
# For deployment module tests
./mvnw test -pl deployment -Dtest=ClassName

# For integration tests
./mvnw test -pl integration-tests -Dtest=ClassName
```

### Build Documentation
```bash
./mvnw install -Pdocs
```

## Module Structure

This is a multi-module Maven project with a standard Quarkus extension structure:

- **runtime/**: Contains the runtime implementation of the JetStream connector
- **deployment/**: Contains build-time configuration and processing for Quarkus
- **integration-tests/**: End-to-end tests using the extension
- **docs/**: Documentation in AsciiDoc format

## Architecture

### Core Components

**JetStreamConnector** (`runtime/src/main/java/.../JetStreamConnector.java`):
- Main entry point implementing both `InboundConnector` and `OutboundConnector`
- Creates publishers for incoming channels and subscribers for outgoing channels
- Manages health reporting for all message processors
- Uses factory pattern to create processor instances

**Message Processors**:
- **MessagePublisherProcessor**: Handles incoming messages from NATS JetStream (pull or push consumers)
- **MessageSubscriberProcessor**: Handles outgoing messages to NATS JetStream
- Located in `runtime/src/main/java/.../processors/`
- Each processor tracks its own health status

**Client Layer** (`runtime/src/main/java/.../client/`):
- **connection/**: Manages NATS connections and configuration
- **context/**: Handles JetStream context creation and management
- **consumer/**: Consumer implementations (pull and push)
- **publisher/**: Message publishing logic
- **stream/**: Stream management and configuration
- **store/**: Key/Value store support
- **tracing/**: OpenTelemetry integration for distributed tracing

**Configuration** (`runtime/src/main/java/.../configuration/`):
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

- **deployment/src/test/**: Unit tests that start a Quarkus application with the extension
- **integration-tests/src/test/**: Integration tests using the full extension (JVM and Native)
- Tests use Quarkus Dev Services to automatically start a NATS container via Testcontainers
- Common test utilities in `deployment/src/test/java/.../test/`

### Dev Services

The extension provides Dev Services support that automatically starts a NATS server container during development and testing. Configuration in `deployment/src/main/java/.../deployment/JetStreamDevServicesProcessor.java`.

### GraalVM Native Support

Native image configuration is handled in `runtime/src/main/java/.../graal/` for reflection and resource registration required by the NATS Java client.

### Annotation Processors

The build uses several annotation processors:
- SmallRye Connector Attribute Processor: Generates connector configuration metadata
- Lombok: For code generation
- Quarkus Extension Processor: Generates extension metadata

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

- **Quarkus**: 3.28.5
- **Java**: 17 (minimum)
- **NATS Java Client**: 2.23.0
- **SmallRye Reactive Messaging**: Provided by Quarkus BOM
- **OpenTelemetry**: For distributed tracing
- **Lombok**: For reducing boilerplate
- **Jackson**: For JSON serialization/deserialization
