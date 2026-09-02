package io.quarkiverse.reactive.messaging.nats.jetstream.connector.deployment;

import static io.quarkiverse.reactive.messaging.nats.jetstream.connector.deployment.JetStreamDevServicesProcessor.DEV_SERVICE_LABEL;
import static io.quarkus.devservices.common.ConfigureUtil.configureSharedServiceLabel;

import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamContainerConfiguration;
import io.quarkus.deployment.builditem.Startable;
import io.quarkus.runtime.LaunchMode;

class JetStreamContainer extends io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamContainer implements Startable {

    JetStreamContainer(DockerImageName imageName, Integer fixedExposedPort, String defaultNetworkId,
            boolean useSharedNetwork, JetStreamContainerConfiguration configuration) {
        super(imageName, fixedExposedPort, defaultNetworkId, useSharedNetwork, configuration);
    }

    public JetStreamContainer withSharedServiceLabel(LaunchMode launchMode, String serviceName) {
        return (JetStreamContainer) configureSharedServiceLabel(this, launchMode, DEV_SERVICE_LABEL, serviceName);
    }

    @Override
    public String getConnectionInfo() {
        return super.getConnectionInfo();
    }
}
