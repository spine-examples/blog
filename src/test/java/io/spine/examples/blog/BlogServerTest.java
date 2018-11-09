package io.spine.examples.blog;

import io.spine.client.QueryResponse;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.commands.CreateBlogPost;
import io.spine.examples.blog.commands.PublishBlogPost;
import io.spine.examples.testutil.TestServerClient;
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
    private TestServerClient client;

    @BeforeEach
    void setup() {
        client = new TestServerClient("localhost", DEFAULT_CLIENT_SERVICE_PORT);
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
        BlogId blogId = newBlogId();
        CreateBlog createBlog = createBlog(blogId, "Test Blog");
        client.post(createBlog);

        BlogPostId blogPostId = newBlogPostId(blogId);
        CreateBlogPost createPost = createBlogPost(blogPostId, "Test Blog Post");
        client.post(createPost);

        QueryResponse blogResponse = client.queryAll(Blog.class);
        assertEquals(1, blogResponse.getMessagesCount());
        Blog blog = (Blog) unpack(blogResponse.getMessages(0));
        assertEquals(blogId, blog.getId());
        assertEquals(createBlog.getName(), blog.getName());
        assertTrue(blog.getPostsList()
                       .contains(blogPostId));

        QueryResponse postResponse = client.queryAll(BlogPost.class);
        assertEquals(1, postResponse.getMessagesCount());
        BlogPost blogPost = (BlogPost) unpack(postResponse.getMessages(0));
        assertEquals(blogPostId, blogPost.getId());
        assertEquals(createPost.getTitle(), blogPost.getTitle());
        assertEquals(BlogPost.Status.DRAFT, blogPost.getStatus());
    }

    @Test
    @DisplayName("BlogView should return a list of published BlogPosts")
    void queryBlogView() {
        BlogId blogId = newBlogId();
        CreateBlog createBlog = createBlog(blogId, "Test Blog");
        client.post(createBlog);

        BlogPostId post1 = newBlogPostId(blogId);
        CreateBlogPost createPost1 = createBlogPost(post1, "Test Blog Post");
        client.post(createPost1);

        BlogPostId post2 = newBlogPostId(blogId);
        CreateBlogPost createPost2 = createBlogPost(post2, "Test Blog Post 2");
        client.post(createPost2);

        PublishBlogPost publishPost2 = PublishBlogPost
                .newBuilder()
                .setBlogPostId(post2)
                .build();
        client.post(publishPost2);

        QueryResponse response = client.queryAll(BlogView.class);
        assertEquals(1, response.getMessagesCount());

        BlogView blogView = (BlogView) unpack(response.getMessages(0));

        assertEquals(blogId, blogView.getBlogId());
        assertEquals(1, blogView.getPostsCount());
        assertEquals(post2, blogView.getPosts(0)
                                    .getId());
    }

    private static CreateBlog createBlog(BlogId blogId, String name) {
        return CreateBlog.newBuilder()
                         .setBlogId(blogId)
                         .setName(name)
                         .build();
    }

    private static CreateBlogPost createBlogPost(BlogPostId postId, String title) {
        return CreateBlogPost.newBuilder()
                             .setBlogPostId(postId)
                             .setTitle(title)
                             .build();
    }
}
