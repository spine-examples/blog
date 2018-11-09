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

import io.spine.logging.Logging;
import io.spine.server.BoundedContext;
import io.spine.server.CommandService;
import io.spine.server.QueryService;
import io.spine.server.transport.GrpcContainer;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

/**
 * A local gRPC {@link BlogServer} running a {@link BlogBoundedContext}.
 *
 * @author Anton Nikulin
 */
public class BlogServer implements Logging {

    private int port;
    private GrpcContainer grpcContainer;
    private BoundedContext boundedContext;

    BlogServer(int port) {
        this.port = port;
        this.boundedContext = BlogBoundedContext.getInstance();
        this.grpcContainer = createGrpcContainer(this.boundedContext);
    }

    public static void main(String[] args) throws IOException {
        int port = getPort();
        BlogServer blogServer = new BlogServer(port);
        blogServer.start();
    }

    private static int getPort() {
        String port = System.getProperty("port", String.valueOf(DEFAULT_CLIENT_SERVICE_PORT));
        return Integer.parseInt(port);
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

        log().info("Server started, listening to commands on the port " + port);

        grpcContainer.awaitTermination();
    }

    void shutdown() throws Exception {
        log().info("Shutting down the server...");
        grpcContainer.shutdown();
        boundedContext.close();
    }
}
