package io.spine.examples.blog;

import io.spine.client.QueryResponse;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.commands.CreateBlogPost;
import io.spine.examples.blog.commands.PublishBlogPost;
import io.spine.examples.kanban.given.KanbanClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.blog.given.TestIdentifiers.newBlogId;
import static io.spine.examples.blog.given.TestIdentifiers.newBlogPostId;
import static io.spine.protobuf.AnyPacker.unpack;
import static org.junit.jupiter.api.Assertions.*;

class BlogServerTest {

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
    void tearDown() throws Exception {
        client.shutdown();
        blogServer.shutdown();
    }

    @Test
    @DisplayName("CreateBlog should create a Blog, and CreateBlog should create a BlogPost")
    void createBlogWithPost() {
        final BlogId blogId = newBlogId();
        final CreateBlog createBlogCommand = createBlogCommand(blogId, "Test Blog");
        client.post(createBlogCommand);

        final BlogPostId blogPostId = newBlogPostId(blogId);
        final CreateBlogPost createBlogPostCommand = createBlogPostCommand(blogPostId, "Test Blog Post");
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

    @Test
    @DisplayName("BlogView should return a list of published BlogPosts")
    void queryBlogView() {
        final BlogId blogId = newBlogId();
        final CreateBlog createBlogCommand = createBlogCommand(blogId, "Test Blog");
        client.post(createBlogCommand);

        final BlogPostId blogPostId = newBlogPostId(blogId);
        final CreateBlogPost createBlogPostCommand = createBlogPostCommand(blogPostId, "Test Blog Post");
        client.post(createBlogPostCommand);

        final BlogPostId blogPostId2 = newBlogPostId(blogId);
        final CreateBlogPost createBlogPostCommand2 = createBlogPostCommand(blogPostId2, "Test Blog Post 2");
        client.post(createBlogPostCommand2);

        final PublishBlogPost publishBlogPostCommand = PublishBlogPost.newBuilder()
                .setBlogPostId(blogPostId2)
                .build();
        client.post(publishBlogPostCommand);

        QueryResponse response = client.queryAll(BlogView.class);
        assertEquals(1, response.getMessagesCount());

        BlogView blogView = unpack(response.getMessages(0));

        assertEquals(blogId, blogView.getBlogId());
        assertEquals(1, blogView.getPostsCount());
        assertEquals(blogPostId2, blogView.getPosts(0).getId());
    }

    private CreateBlog createBlogCommand(BlogId blogId, String name) {
        return CreateBlog.newBuilder()
                .setBlogId(blogId)
                .setName(name)
                .build();
    }

    private CreateBlogPost createBlogPostCommand(BlogPostId blogPostId, String title) {
        return CreateBlogPost.newBuilder()
                .setBlogPostId(blogPostId)
                .setTitle(title)
                .build();
    }
}
