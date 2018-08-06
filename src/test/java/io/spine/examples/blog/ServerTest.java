package io.spine.examples.blog;

import io.spine.client.QueryResponse;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.commands.CreatePost;
import io.spine.examples.kanban.given.KanbanClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.spine.base.Identifier.newUuid;
import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.protobuf.AnyPacker.unpack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerTest {

    private Server server;
    private KanbanClient client;

    @BeforeEach
    void setup() {
        client = new KanbanClient("localhost", DEFAULT_CLIENT_SERVICE_PORT);
        server = new Server(DEFAULT_CLIENT_SERVICE_PORT);
        new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        client.shutdown();
        server.shutdown();
    }

    @Test
    void createBlogWithPost() {
        final BlogId blogId = BlogId.newBuilder()
                .setValue(newUuid())
                .build();
        final CreateBlog createBlogCommand = CreateBlog.newBuilder()
                .setBlogId(blogId)
                .setName("Test Blog")
                .build();
        client.post(createBlogCommand);

        final PostId postId = PostId.newBuilder()
                .setValue(newUuid())
                .build();
        final CreatePost createPostCommand = CreatePost.newBuilder()
                .setPostId(postId)
                .setBlogId(blogId)
                .setTitle("Test Blog Post")
                .build();
        client.post(createPostCommand);

        QueryResponse blogResponse = client.queryAll(Blog.class);
        assertEquals(1, blogResponse.getMessagesCount());
        Blog blog = unpack(blogResponse.getMessages(0));
        assertEquals(blogId, blog.getId());
        assertEquals(createBlogCommand.getName(), blog.getName());
        assertTrue(blog.getPostsList().contains(postId));

        QueryResponse postResponse = client.queryAll(Post.class);
        assertEquals(1, postResponse.getMessagesCount());
        Post post = unpack(postResponse.getMessages(0));
        assertEquals(postId, post.getId());
        assertEquals(createPostCommand.getTitle(), post.getTitle());
        assertEquals(Post.Status.DRAFT, post.getStatus());
    }
}
