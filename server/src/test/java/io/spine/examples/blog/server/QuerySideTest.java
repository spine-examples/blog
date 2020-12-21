/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.BlogView;
import io.spine.examples.blog.PostId;
import io.spine.examples.blog.PostItem;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.commands.CreatePost;
import io.spine.examples.blog.commands.PublishPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.protobuf.AnyPacker.unpack;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Blog Query Side should")
class QuerySideTest extends BlogServerTest {

    private final BlogId blog = BlogId.generate();
    private PostId publishedPost;

    @BeforeEach
    void setUp() {
        CreateBlog createBlog = createBlog(blog, "Query Side Blog Test");
        send(createBlog);

        PostId nonPublishedPost = PostId.generate();
        CreatePost createPost1 = createPost(nonPublishedPost, blog, "Draft Post");
        send(createPost1);

        publishedPost = PostId.generate();
        CreatePost createPost2 = createPost(publishedPost, blog, "Published Post");
        send(createPost2);

        PublishPost publishPost2 = PublishPost
                .newBuilder()
                .setPost(publishedPost)
                .setBlog(blog)
                .build();
        send(publishPost2);
    }

    @Test
    @DisplayName("return a list of published blog posts")
    void queryBlogView() {
        QueryResponse response = queryAll(BlogView.class);
        assertEquals(1, response.getMessageCount());

        BlogView expected = BlogView
                .newBuilder()
                .setId(blog)
                .addPost(PostItem.newBuilder()
                                 .setId(publishedPost))
                .buildPartial();

        BlogView blogView = (BlogView) unpack(response.getMessage(0).getState());
        assertThat(blogView)
                .comparingExpectedFieldsOnly()
                .isEqualTo(expected);
    }
}
