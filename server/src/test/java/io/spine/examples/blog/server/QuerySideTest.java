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

import com.google.common.truth.Truth;
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

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;

@DisplayName("Blog Query Side should")
class QuerySideTest extends BlogServerTest {

    private final BlogId blogId = BlogId.generate();
    private PostId publishedPost;

    @BeforeEach
    void setUp() {
        CreateBlog createBlog = createBlog(blogId, "Query Side Blog Test");
        post(createBlog);

        PostId nonPublishedPost = PostId.generate();
        CreatePost createPost1 = createPost(nonPublishedPost, blogId, "Post 1");
        post(createPost1);

        publishedPost = PostId.generate();
        CreatePost createPost2 = createPost(publishedPost, blogId, "Post 2");
        post(createPost2);

        PublishPost publishPost2 = PublishPost
                .newBuilder()
                .setPost(publishedPost)
                .setBlog(blogId)
                .vBuild();
        post(publishPost2);
    }

    @Test
    @DisplayName("return a list of published blog posts")
    void queryBlogView() {
        QueryResponse response = queryAll(BlogView.class);

        Truth.assertThat(response.size())
             .isEqualTo(1);

        BlogView blogView = (BlogView) response.state(0);

        assertThat(blogView.getId())
                .isEqualTo(blogId);
        assertThat(blogView.getPostList())
                .hasSize(1);
        assertThat(blogView.getPost(0).getId())
                .isEqualTo(publishedPost);
    }
}
