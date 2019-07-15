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

import com.google.protobuf.Message;
import io.spine.base.CommandMessage;
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

import static io.spine.testing.TestValues.randomString;
import static io.spine.testing.core.given.GivenUserId.newUuid;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Abstract base for integration tests.
 */
@DisplayName("BlogServer")
public abstract class BlogServerTest {

    private Server server;
    private TestClient client;

    @BeforeEach
    void setup() {
        server = BlogServer.create();
        client = new TestClient(newUuid(), "localhost", server.port());
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

    protected final void post(CommandMessage command) {
        client.post(command);
    }

    final QueryResponse queryAll(Class<? extends Message> messageType) {
        return client.queryAll(messageType);
    }

    static CreateBlog createBlog(BlogId blogId, String name) {
        return CreateBlog
                .newBuilder()
                .setBlogId(blogId)
                .setTitle(name)
                .build();
    }

    static CreatePost createPost(PostId postId, String title) {
        return CreatePost
                .newBuilder()
                .setPostId(postId)
                .setTitle(title)
                .setBody("Generated " + randomString() + " post body.")
                .build();
    }
}
