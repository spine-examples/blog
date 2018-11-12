package io.spine.examples.blog;

import com.google.protobuf.Message;
import io.spine.base.CommandMessage;
import io.spine.client.QueryResponse;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.commands.CreateBlogPost;
import io.spine.examples.testutil.TestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("BlogServer")
public class BlogServerTest {

    private BlogServer server;
    private TestClient client;

    @BeforeEach
    void setup() {
        int port = DEFAULT_CLIENT_SERVICE_PORT;
        client = new TestClient("localhost", port);
        server = new BlogServer(port);
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                this.server.start();
            } catch (IOException e) {
                fail(e);
            }
        }).start();
    }

    @AfterEach
    void tearDown() throws Exception {
        client.shutdown();
        server.shutdown();
    }

    protected final void post(CommandMessage command) {
        client.post(command);
    }

    protected final QueryResponse queryAll(Class<? extends Message> messageType) {
        return client.queryAll(messageType);
    }

    protected static CreateBlog createBlog(BlogId blogId, String name) {
        return CreateBlog
                .newBuilder()
                .setBlogId(blogId)
                .setName(name)
                .build();
    }

    protected static CreateBlogPost createBlogPost(BlogPostId postId, String title) {
        return CreateBlogPost
                .newBuilder()
                .setBlogPostId(postId)
                .setTitle(title)
                .build();
    }
}
