package io.spine.examples.blog;

import io.spine.client.QueryResponse;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.commands.CreateBlogPost;
import io.spine.examples.kanban.given.KanbanClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.blog.given.TestIdentifiers.newBlogId;
import static io.spine.examples.blog.given.TestIdentifiers.newBlogPostId;
import static io.spine.protobuf.AnyPacker.unpack;
import static org.junit.jupiter.api.Assertions.*;

public class BlogServerTest {

    private BlogServer blogServer;
    private KanbanClient client;

    @BeforeEach
    void setup() {
        client = new KanbanClient("localhost", DEFAULT_CLIENT_SERVICE_PORT);
        blogServer = new BlogServer(DEFAULT_CLIENT_SERVICE_PORT);
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                this.blogServer.start();
            } catch (IOException e) {
                fail(e);
            }
        }).start();
    }


    @AfterEach
    void tearDown() throws InterruptedException {
        client.shutdown();
        blogServer.shutdown();
    }

    @Test
    void createBlogWithPost() {
        final BlogId blogId = newBlogId();
        final CreateBlog createBlogCommand = CreateBlog.newBuilder()
                .setBlogId(blogId)
                .setName("Test Blog")
                .build();
        client.post(createBlogCommand);

        final BlogPostId blogPostId = newBlogPostId();
        final CreateBlogPost createBlogPostCommand = CreateBlogPost.newBuilder()
                .setBlogPostId(blogPostId)
                .setBlogId(blogId)
                .setTitle("Test Blog Post")
                .build();
        client.post(createBlogPostCommand);

        QueryResponse blogResponse = client.queryAll(Blog.class);
        assertEquals(1, blogResponse.getMessagesCount());
        Blog blog = unpack(blogResponse.getMessages(0));
        assertEquals(blogId, blog.getId());
        assertEquals(createBlogCommand.getName(), blog.getName());
        assertTrue(blog.getPostsList().contains(blogPostId));

        QueryResponse postResponse = client.queryAll(BlogPost.class);
        assertEquals(1, postResponse.getMessagesCount());
        BlogPost blogPost = unpack(postResponse.getMessages(0));
        assertEquals(blogPostId, blogPost.getId());
        assertEquals(createBlogPostCommand.getTitle(), blogPost.getTitle());
        assertEquals(BlogPost.Status.DRAFT, blogPost.getStatus());
    }
}
