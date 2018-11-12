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

package io.spine.examples.blog.server.c;

import io.spine.client.QueryResponse;
import io.spine.examples.blog.Blog;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.Post;
import io.spine.examples.blog.PostId;
import io.spine.examples.blog.server.BlogServerTest;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.commands.CreatePost;
import io.spine.testing.server.ShardingReset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.spine.examples.blog.given.TestIdentifiers.newBlogId;
import static io.spine.examples.blog.given.TestIdentifiers.newPostId;
import static io.spine.protobuf.AnyPacker.unpack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Blog Command Side should")
@ExtendWith(ShardingReset.class)
class CommandSideTest extends BlogServerTest {

    /** ID of the blog we're creating. */
    private final BlogId blogId = newBlogId();
    /** The command message to create the blog. */
    private CreateBlog createBlog;
    /** The ID of the post we create in the blog. */
    private PostId postId;
    /** The command to create the blog post. */
    private CreatePost createPost;

    @BeforeEach
    void setUp() {
        createBlog = createBlog(blogId, "Server Side Blog Test");
        post(createBlog);

        postId = newPostId(blogId);
        createPost = createPost(postId, "Server Blog Post");
        post(createPost);
    }

    @Test
    @DisplayName("create a blog")
    void createsBlog() {
        QueryResponse blogResponse = queryAll(Blog.class);
        assertEquals(1, blogResponse.getMessagesCount());
        Blog blog = (Blog) unpack(blogResponse.getMessages(0));
        assertEquals(blogId, blog.getId());
        assertEquals(createBlog.getTitle(), blog.getTitle());
        assertTrue(blog.getPostsList()
                       .contains(postId));
    }

    @Test
    @DisplayName("create a blog post")
    void createsPost() {
        QueryResponse postResponse = queryAll(Post.class);
        assertEquals(1, postResponse.getMessagesCount());
        Post blogPost = (Post) unpack(postResponse.getMessages(0));
        assertEquals(postId, blogPost.getId());
        assertEquals(createPost.getTitle(), blogPost.getTitle());
        assertEquals(Post.Status.DRAFT, blogPost.getStatus());
    }
}
