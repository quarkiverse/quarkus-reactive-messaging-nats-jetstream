package io.quarkiverse.reactive.nats.jetstream;

import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface Client {

    Uni<Message> publish(Message message, String stream, String subject);

    Multi<Message> publish(Multi<Message> messages, String stream, String subject);


    /**


    @NonNull
    Uni<Dispatcher> createDispatcher(@Nullable MessageHandler handler);

    @NonNull
    Uni<Dispatcher> createDispatcher();

    @NonNull
    Uni<Void> closeDispatcher(@NonNull Dispatcher dispatcher);

    @NonNull
    Uni<Void> addConnectionListener(@NonNull ConnectionListener connectionListener);

    @NonNull
    Uni<Void> removeConnectionListener(@NonNull ConnectionListener connectionListener);

    @NonNull
    Uni<Void> flush(@Nullable Duration timeout);

    @NonNull
    Uni<Boolean> drain(@Nullable Duration timeout);

    @NonNull
    Uni<Void> close();

    @NonNull
    Uni<Status> getStatus();

    @NonNull
    Uni<ServerInfo> getServerInfo();

    @NonNull
    Uni<String> getConnectedUrl();

    @NonNull
    Uni<Void> flushBuffer();


     */




}
