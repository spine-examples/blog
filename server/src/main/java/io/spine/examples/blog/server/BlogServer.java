/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import io.spine.base.Environment;
import io.spine.base.EnvironmentType;
import io.spine.examples.blog.server.blog.BlogRepository;
import io.spine.examples.blog.server.blog.BlogViewRepository;
import io.spine.examples.blog.server.post.PostAggregate;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.server.Server;
import io.spine.server.ServerEnvironment;
import io.spine.server.storage.memory.InMemoryStorageFactory;
import io.spine.server.transport.memory.InMemoryTransportFactory;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

/**
 * A local gRPC {@link BlogServer} running the Blog Bounded Context.
 */
public class BlogServer {

    /** Prevents instantiation. */
    private BlogServer() {
    }

    static Server create() {
        ServerEnvironment serverEnvironment = ServerEnvironment.instance();
        Environment env = Environment.instance();
        Class<? extends EnvironmentType> envType = env.type();
        serverEnvironment.use(InMemoryStorageFactory.newInstance(), envType);
        serverEnvironment.use(InMemoryTransportFactory.newInstance(), envType);

        BoundedContextBuilder context = BoundedContext
                .singleTenant("Blog")
                .add(new BlogRepository())
                .add(PostAggregate.class)
                .add(new BlogViewRepository());
        return Server
                .atPort(DEFAULT_CLIENT_SERVICE_PORT)
                .add(context)
                .build();
    }

    public static void main(String[] args) throws IOException {
        Server server = create();
        server.start();
        server.awaitTermination();
    }
}
