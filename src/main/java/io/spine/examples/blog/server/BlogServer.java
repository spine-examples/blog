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

package io.spine.examples.blog.server;

import io.spine.core.BoundedContextName;
import io.spine.examples.blog.server.c.BlogRepository;
import io.spine.examples.blog.server.c.PostRepository;
import io.spine.examples.blog.server.q.BlogViewRepository;
import io.spine.logging.Logging;
import io.spine.server.BoundedContext;
import io.spine.server.CommandService;
import io.spine.server.QueryService;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.memory.InMemoryStorageFactory;
import io.spine.server.transport.GrpcContainer;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

/**
 * A local gRPC {@link BlogServer} running the Blog Bounded Context.
 */
public class BlogServer implements Logging {

    static final BoundedContextName contextName = BoundedContextName
            .newBuilder()
            .setValue("Blog")
            .build();

    private final int port;
    private final GrpcContainer grpcContainer;
    private final BoundedContext boundedContext;

    BlogServer(StorageFactory storageFactory, int port) {
        this.port = port;
        this.boundedContext = createBoundedContext(storageFactory);
        this.grpcContainer = createGrpcContainer(this.boundedContext);
    }

    public static void main(String[] args) throws IOException {
        int port = getPort();
        StorageFactory storageFactory = InMemoryStorageFactory.newInstance(contextName, false);

        BlogServer blogServer = new BlogServer(storageFactory, port);
        blogServer.start();
    }

    private static int getPort() {
        @SuppressWarnings("AccessOfSystemProperties") // No security risk.
        String port = System.getProperty("port", String.valueOf(DEFAULT_CLIENT_SERVICE_PORT));
        return Integer.parseInt(port);
    }

    private static BoundedContext createBoundedContext(StorageFactory storageFactory) {
        BoundedContext context = BoundedContext
                .newBuilder()
                .setName(contextName)
                .setStorageFactorySupplier(() -> storageFactory)
                .build();

        context.register(new BlogRepository());
        context.register(new PostRepository());
        context.register(new BlogViewRepository());
        return context;
    }

    private GrpcContainer createGrpcContainer(BoundedContext boundedContext) {
        CommandService commandService = CommandService
                .newBuilder()
                .add(boundedContext)
                .build();
        QueryService queryService = QueryService
                .newBuilder()
                .add(boundedContext)
                .build();

        GrpcContainer.Builder result = GrpcContainer.newBuilder()
                .setPort(port)
                .addService(commandService)
                .addService(queryService);
        return result.build();
    }

    public void start() throws IOException {
        grpcContainer.start();
        grpcContainer.addShutdownHook();

        log().info("Server started, listening to commands on the port {}", port);

        grpcContainer.awaitTermination();
    }

    void shutdown() throws Exception {
        log().info("Shutting down the server...");
        grpcContainer.shutdown();
        boundedContext.close();
    }
}