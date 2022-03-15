/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import io.spine.base.CommandMessage;
import io.spine.base.EntityState;
import io.spine.client.QueryResponse;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.PostId;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.commands.CreatePost;
import io.spine.server.Server;
import io.spine.testing.client.grpc.TestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.testing.TestValues.randomString;
import static io.spine.testing.core.given.GivenUserId.newUuid;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Abstract base for integration tests.
 */
@DisplayName("BlogServer")
abstract class BlogServerTest {

    private Server server;
    private TestClient client;

    @BeforeEach
    void setup() {
        server = BlogServer.create();
        client = new TestClient(newUuid(), "localhost", DEFAULT_CLIENT_SERVICE_PORT);
        startServer();
    }

    private void startServer() {
        try {
            this.server.start();
        } catch (IOException e) {
            fail(e);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        client.shutdown();
        server.shutdownAndWait();
    }

    final void send(CommandMessage command) {
        client.post(command);
    }

    final QueryResponse queryAll(Class<? extends EntityState> stateType) {
        return client.queryAll(stateType);
    }

    static CreateBlog createBlog(BlogId id, String name) {
        return CreateBlog
                .newBuilder()
                .setId(id)
                .setTitle(name)
                .vBuild();
    }

    static CreatePost createPost(PostId id, BlogId blog, String title) {
        return CreatePost
                .newBuilder()
                .setId(id)
                .setBlog(blog)
                .setTitle(title)
                .setBody("Generated " + randomString() + " post body.")
                .vBuild();
    }
}
