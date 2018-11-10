package io.spine.examples.blog;

import io.spine.client.QueryResponse;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.commands.CreateBlogPost;
import io.spine.examples.blog.commands.PublishBlogPost;
import io.spine.examples.testutil.TestServerClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.blog.given.TestIdentifiers.newBlogId;
import static io.spine.examples.blog.given.TestIdentifiers.newBlogPostId;
import static io.spine.protobuf.AnyPacker.unpack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("BlogServer")
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

    @Nested
    @DisplayName("command side")
    class CommandSide {

        /** ID of the blog we're creating. */
        private final BlogId blogId = newBlogId();
        /** The command message to create the blog. */
        private CreateBlog createBlog;
        /** The ID of the post we create in the blog. */
        private BlogPostId postId;
        /** The command to create the blog post. */
        private CreateBlogPost createPost;

        @BeforeEach
        void setUp() {
            createBlog = createBlog(blogId, "Server Side Blog Test");
            client.post(createBlog);

            postId = newBlogPostId(blogId);
            createPost = createBlogPost(postId, "Server Blog Post");
            client.post(createPost);
        }

        @Test
        @DisplayName("create a blog")
        void createsBlog() {
            QueryResponse blogResponse = client.queryAll(Blog.class);
            assertEquals(1, blogResponse.getMessagesCount());
            Blog blog = (Blog) unpack(blogResponse.getMessages(0));
            assertEquals(blogId, blog.getId());
            assertEquals(createBlog.getName(), blog.getName());
            assertTrue(blog.getPostsList()
                           .contains(postId));
        }

        @Test
        @DisplayName("create a blog post")
        void createsPost() {
            QueryResponse postResponse = client.queryAll(BlogPost.class);
            assertEquals(1, postResponse.getMessagesCount());
            BlogPost blogPost = (BlogPost) unpack(postResponse.getMessages(0));
            assertEquals(postId, blogPost.getId());
            assertEquals(createPost.getTitle(), blogPost.getTitle());
            assertEquals(BlogPost.Status.DRAFT, blogPost.getStatus());
        }
    }

    @Nested
    @DisplayName("query side should")
    class QuerySide {

        private final BlogId blogId = newBlogId();
        private BlogPostId post1;
        private BlogPostId post2;

        @BeforeEach
        void setUp() {
            CreateBlog createBlog = createBlog(blogId, "Query Side Blog Test");
            client.post(createBlog);

            post1 = newBlogPostId(blogId);
            CreateBlogPost createPost1 = createBlogPost(post1, "Post 1");
            client.post(createPost1);

            post2 = newBlogPostId(blogId);
            CreateBlogPost createPost2 = createBlogPost(post2, "Post 2");
            client.post(createPost2);

            PublishBlogPost publishPost2 = PublishBlogPost
                    .newBuilder()
                    .setBlogPostId(post2)
                    .build();
            client.post(publishPost2);
        }

        @Test
        @DisplayName("return a list of published blog posts")
        void queryBlogView() {
            QueryResponse response = client.queryAll(BlogView.class);
            assertEquals(1, response.getMessagesCount());

            BlogView blogView = (BlogView) unpack(response.getMessages(0));

            assertEquals(blogId, blogView.getBlogId());
            assertEquals(1, blogView.getPostsCount());
            assertEquals(post2, blogView.getPosts(0)
                                        .getId());
        }
    }

    private static CreateBlog createBlog(BlogId blogId, String name) {
        return CreateBlog
                .newBuilder()
                .setBlogId(blogId)
                .setName(name)
                .build();
    }

    private static CreateBlogPost createBlogPost(BlogPostId postId, String title) {
        return CreateBlogPost
                .newBuilder()
                .setBlogPostId(postId)
                .setTitle(title)
                .build();
    }
}
