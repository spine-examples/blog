/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.examples.blog.server.post;

import io.spine.examples.blog.Post;
import io.spine.examples.blog.Post.Status;
import io.spine.examples.blog.PostId;
import io.spine.examples.blog.commands.CreatePost;
import io.spine.examples.blog.commands.PublishPost;
import io.spine.examples.blog.events.PostCreated;
import io.spine.examples.blog.events.PostPublished;
import io.spine.examples.blog.rejections.CannotPublishPost;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * A post aggregate handles commands related to a blog post.
 */
public final class PostAggregate extends Aggregate<PostId, Post, Post.Builder> {

    PostAggregate(PostId id) {
        super(id);
    }

    @Assign
    PostCreated handle(CreatePost c) {
        return PostCreated
                .newBuilder()
                .setId(c.getId())
                .setBlog(c.getBlog())
                .setTitle(c.getTitle())
                .setBody(c.getBody())
                .vBuild();
    }

    @Assign
    PostPublished handle(PublishPost c) throws CannotPublishPost {
        Post post = state();
        Status status = post.getStatus();
        PostId postId = c.getPost();
        if (status != Status.DRAFT) {
            boolean published = status == Status.PUBLISHED;
            throw CannotPublishPost
                    .newBuilder()
                    .setPostId(postId)
                    .setAlreadyPublished(published)
                    .setAlreadyDeleted(!published)
                    .build();
        }
        return PostPublished
                .newBuilder()
                .setPost(postId)
                .setBlog(c.getBlog())
                .setTitle(post.getTitle())
                .setBody(post.getBody())
                .vBuild();
    }

    @Apply
    private void event(PostCreated e) {
        builder().setTitle(e.getTitle())
                 .setBody(e.getBody())
                 .setStatus(Status.DRAFT);
    }

    @Apply
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void event(PostPublished e) {
        builder().setStatus(Status.PUBLISHED);
    }
}
