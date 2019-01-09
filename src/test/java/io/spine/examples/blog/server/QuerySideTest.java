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

import io.spine.client.QueryResponse;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.BlogView;
import io.spine.examples.blog.PostId;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.commands.CreatePost;
import io.spine.examples.blog.commands.PublishPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.blog.given.TestIdentifiers.newBlogId;
import static io.spine.examples.blog.given.TestIdentifiers.newPostId;
import static io.spine.protobuf.AnyPacker.unpack;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Blog Query Side should")
class QuerySideTest extends BlogServerTest {

    private final BlogId blogId = newBlogId();
    private PostId post1;
    private PostId post2;

    @BeforeEach
    void setUp() {
        CreateBlog createBlog = createBlog(blogId, "Query Side Blog Test");
        post(createBlog);

        post1 = newPostId(blogId);
        CreatePost createPost1 = createPost(post1, "Post 1");
        post(createPost1);

        post2 = newPostId(blogId);
        CreatePost createPost2 = createPost(post2, "Post 2");
        post(createPost2);

        PublishPost publishPost2 = PublishPost
                .newBuilder()
                .setPostId(post2)
                .build();
        post(publishPost2);
    }

    @Test
    @DisplayName("return a list of published blog posts")
    void queryBlogView() {
        QueryResponse response = queryAll(BlogView.class);
        assertEquals(1, response.getMessagesCount());

        BlogView blogView = (BlogView) unpack(response.getMessages(0));

        assertEquals(blogId, blogView.getBlogId());
        assertEquals(1, blogView.getPostsCount());
        assertEquals(post2, blogView.getPosts(0)
                                    .getId());
    }
}
