/*
 * Copyright 2018, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.examples.blog;

import io.spine.core.BoundedContextName;
import io.spine.logging.Logging;
import io.spine.server.BoundedContext;
import io.spine.server.CommandService;
import io.spine.server.QueryService;
import io.spine.server.event.EventBus;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.memory.InMemoryStorageFactory;
import io.spine.server.transport.GrpcContainer;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Supplier;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

/**
 * A local gRPC {@link BlogServer} with {@link InMemoryStorageFactory} running a Blog Bounded Context.
 *
 * @author Anton Nikulin
 */
public class BlogServer {

    private static final String BOUNDED_CONTEXT_NAME = "Blog";

    private final int port;
    private final GrpcContainer grpcContainer;
    private final BoundedContext boundedContext;
    private final Supplier<Logger> loggerSupplier = Logging.supplyFor(getClass());

    public BlogServer(int port) {
        this.port = port;
        this.boundedContext = createBoundedContext();
        this.grpcContainer = createGrpcContainer(this.boundedContext);
    }

    private BoundedContext createBoundedContext() {
        final BlogPostAggregateRepository blogPostRepo = new BlogPostAggregateRepository();

        final BoundedContextName name = BoundedContextName.newBuilder()
                .setValue(BOUNDED_CONTEXT_NAME)
                .build();
        final StorageFactory storageFactory = InMemoryStorageFactory.newInstance(name, false);


        final BoundedContext context = BoundedContext.newBuilder()
                .setName(name)
                .setEventBus(createEventBus(storageFactory, blogPostRepo))
                .setStorageFactorySupplier(() -> storageFactory)
                .build();
        context.register(new BlogAggregateRepository());
        context.register(blogPostRepo);
        context.register(new BlogViewRepository());

        return context;
    }

    private static EventBus.Builder createEventBus(StorageFactory storageFactory,
                                                   BlogPostAggregateRepository blogPostRepo) {
        BlogEnrichments blogEnrichments = BlogEnrichments.newBuilder().setBlogPostRepository(blogPostRepo).build();
        return EventBus.newBuilder()
                .setEnricher(blogEnrichments.createEventEnricher())
                .setStorageFactory(storageFactory);
    }

    private GrpcContainer createGrpcContainer(BoundedContext boundedContext) {
        final CommandService commandService = CommandService.newBuilder()
                .add(boundedContext)
                .build();
        final QueryService queryService = QueryService.newBuilder()
                .add(boundedContext)
                .build();

        final GrpcContainer.Builder result = GrpcContainer.newBuilder()
                .setPort(port)
                .addService(commandService)
                .addService(queryService);
        return result.build();
    }

    public void start() throws IOException {
        grpcContainer.start();
        grpcContainer.addShutdownHook();

        log().info("Server started, listening to commands on the port " + port);

        grpcContainer.awaitTermination();
    }

    public void shutdown() throws Exception {
        log().info("Shutting down the server...");
        grpcContainer.shutdown();
        boundedContext.close();
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getProperty("port", String.valueOf(DEFAULT_CLIENT_SERVICE_PORT)));
        BlogServer blogServer = new BlogServer(port);
        blogServer.start();
    }

    private Logger log() {
        return loggerSupplier.get();
    }
}
