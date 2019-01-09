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

package io.spine.examples.blog.server.post;

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.blog.Post;
import io.spine.examples.blog.Post.Status;
import io.spine.examples.blog.PostId;
import io.spine.examples.blog.PostVBuilder;
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
class PostAggregate extends Aggregate<PostId, Post, PostVBuilder> {

    @VisibleForTesting
    PostAggregate(PostId id) {
        super(id);
    }

    @Assign
    PostCreated handle(CreatePost cmd) {
        return PostCreated.newBuilder()
                .setPostId(cmd.getPostId())
                .setTitle(cmd.getTitle())
                .setBody(cmd.getBody())
                .build();
    }

    @Assign
    PostPublished handle(PublishPost cmd) throws CannotPublishPost {
        Post post = getState();
        Status status = post.getStatus();
        PostId postId = cmd.getPostId();
        if (status != Status.DRAFT) {
            boolean published = status == Status.PUBLISHED;
            throw CannotPublishPost
                    .newBuilder()
                    .setPostId(postId)
                    .setAlreadyPublished(published)
                    .setAlreadyDeleted(!published)
                    .build();
        }
        return PostPublished.newBuilder()
                .setPostId(postId)
                .setTitle(post.getTitle())
                .setBody(post.getBody())
                .build();
    }

    @Apply
    void blogPostCreated(PostCreated event) {
        getBuilder().setId(event.getPostId())
                    .setTitle(event.getTitle())
                    .setBody(event.getBody())
                    .setStatus(Status.DRAFT);
    }

    @Apply
    void blogPostPublished(PostPublished event) {
        getBuilder().setStatus(Status.PUBLISHED);
    }
}
