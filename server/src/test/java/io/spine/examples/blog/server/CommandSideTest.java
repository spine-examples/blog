/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import io.spine.examples.blog.Blog;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.Post;
import io.spine.examples.blog.PostId;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.commands.CreatePost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.protobuf.AnyPacker.unpack;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Blog Command Side should")
class CommandSideTest extends BlogServerTest {

    /** ID of the blog we're creating. */
    private final BlogId blogId = BlogId.generate();

    /** The command message to create the blog. */
    private CreateBlog createBlog;

    /** The ID of the post we create in the blog. */
    private PostId postId;

    /** The command to create the blog post. */
    private CreatePost createPost;

    @BeforeEach
    void setUp() {
        createBlog = createBlog(blogId, "Server Side Blog Test");
        send(createBlog);

        postId = PostId.generate();
        createPost = createPost(postId, blogId, "Server Blog Post");
        send(createPost);
    }

    @Test
    @DisplayName("create a blog")
    void createsBlog() {
        QueryResponse blogResponse = queryAll(Blog.class);
        assertThat(blogResponse.getMessageList())
                .hasSize(1);

        Blog expected = Blog
                .newBuilder()
                .setId(blogId)
                .setTitle(createBlog.getTitle())
                .addPost(postId)
                .build();

        Blog blog = (Blog) unpack(blogResponse.getMessage(0).getState());
        assertThat(blog)
                .comparingExpectedFieldsOnly()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("create a blog post")
    void createsPost() {
        QueryResponse postResponse = queryAll(Post.class);
        assertEquals(1, postResponse.getMessageCount());

        Post expected = Post
                .newBuilder()
                .setId(postId)
                .setTitle(createPost.getTitle())
                .setStatus(Post.Status.DRAFT)
                .build();

        Post blogPost = (Post) unpack(postResponse.getMessage(0).getState());
        assertThat(blogPost)
                .comparingExpectedFieldsOnly()
                .isEqualTo(expected);
    }
}
