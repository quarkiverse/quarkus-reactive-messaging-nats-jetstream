# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Quarkus extension that provides NATS JetStream integration for reactive messaging. It implements the SmallRye Reactive Messaging connector pattern to enable asynchronous message processing with NATS JetStream.

## Build Commands

```bash
# Build the entire project
./mvnw clean compile

# Run tests
./mvnw test

# Run tests for specific module
./mvnw test -pl deployment
./mvnw test -pl runtime
./mvnw test -pl integration-tests

# Run single test
./mvnw test -Dtest=FetchMessagesTest

# Package the extension
./mvnw clean package

# Install to local repository
./mvnw clean install

# Run integration tests
./mvnw verify -Pit

# Build documentation
./mvnw clean compile -Pdocs
```

## Architecture

### Multi-Module Structure
- **runtime/**: Core extension runtime classes and configuration
- **deployment/**: Quarkus build-time processing and configuration
- **integration-tests/**: Integration tests with real NATS JetStream
- **docs/**: Antora-based documentation

### Key Components

#### JetStreamConnector (`runtime/src/main/java/.../JetStreamConnector.java`)
- Main SmallRye Reactive Messaging connector implementation
- Handles both inbound (`Flow.Publisher`) and outbound (`Flow.Subscriber`) messaging
- Manages processor lifecycle and health reporting
- Connector name: `quarkus-jetstream`

#### Message Processing Architecture
- **MessageProcessor**: Base interface for message processing lifecycle
- **MessagePublisherProcessor**: Handles outbound messages (publishing to NATS)
- **MessageSubscriberProcessor**: Handles inbound messages (subscribing from NATS)
- **MessagePublisherProcessorFactory** / **MessageSubscriberProcessorFactory**: Create processors based on channel configuration

#### Client Layer (`runtime/src/main/java/.../client/`)
- **Connection**: Abstraction over NATS connection with JetStream context
- **DefaultConnection**: Primary implementation using NATS Java client
- **StreamManagement**: Manages JetStream streams and consumers
- **KeyValueStoreManagement**: Manages NATS Key-Value stores
- **PullSubscription** / **PushSubscription**: Different subscription patterns

#### Configuration System
- **NatsConfiguration**: Global NATS connection configuration
- **ConnectionConfiguration**: Per-connection configuration
- **ConsumerConfiguration**: Consumer-specific configuration (pull/push/fetch)
- **StreamConfiguration**: JetStream stream setup configuration
- **PublishConfiguration**: Publishing configuration

### Message Flow
1. **Outbound**: Application → MessageSubscriberProcessor → Connection → NATS JetStream
2. **Inbound**: NATS JetStream → Connection → MessagePublisherProcessor → Application

## Configuration

### Global NATS Configuration
```properties
quarkus.messaging.nats.servers=nats://localhost:4222
quarkus.messaging.nats.username=user
quarkus.messaging.nats.password=pass
```

### Channel Configuration
```properties
# Outbound (publishing)
mp.messaging.outgoing.my-channel.connector=quarkus-jetstream
mp.messaging.outgoing.my-channel.stream=my-stream
mp.messaging.outgoing.my-channel.subject=my-subject

# Inbound (subscribing)
mp.messaging.incoming.my-channel.connector=quarkus-jetstream
mp.messaging.incoming.my-channel.stream=my-stream
mp.messaging.incoming.my-channel.subject=my-subject
mp.messaging.incoming.my-channel.publisher-type=Pull
```

### Stream Auto-Configuration
```properties
quarkus.messaging.nats.jet-stream.auto-configure=true
quarkus.messaging.nats.jet-stream.streams[0].name=my-stream
quarkus.messaging.nats.jet-stream.streams[0].subjects[0]=my-subject
```

## Testing

### Test Structure
- **deployment/src/test/**: Unit tests for build-time processing
- **runtime/src/test/**: Unit tests for runtime components
- **integration-tests/**: Full integration tests with test containers

### Test Configuration Files
- Test configurations in `deployment/src/test/resources/application-*.properties`
- Each test scenario has its own application properties file
- Main test config: `application.properties` sets up streams and channels

### Running Specific Tests
```bash
# Run all tests in a specific class
./mvnw test -Dtest=StreamSetupTest

# Run specific test method
./mvnw test -Dtest=FetchMessagesTest#testFetchMessages

# Run tests with specific profile
./mvnw test -Dquarkus.test.profile=fetch
```

## Development Notes

### Lombok Configuration
- `lombok.config` enables generated annotation and stops bubbling
- Lombok used extensively for builders and data classes

### MapStruct Integration
- ConsumerMapper and StreamStateMapper use MapStruct for object mapping
- Generated mappers handle conversion between NATS client objects and extension objects

### Tracing Support
- OpenTelemetry integration for distributed tracing
- Separate tracers for publish and subscribe operations
- Configurable via `trace-enabled` channel attribute

### Native Image Support
- GraalVM native image metadata in `graal/Reflection.java`
- Runtime initialization of secure random classes for NATS client
- SSL/TLS support enabled for native compilation

### Error Handling
- Custom exception hierarchy for different error types
- Dead letter queue support via JetStream advisory messages
- Configurable retry and backoff strategies