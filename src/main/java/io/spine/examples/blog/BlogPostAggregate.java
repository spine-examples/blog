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

package io.spine.examples.blog;

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.blog.BlogPost.Status;
import io.spine.examples.blog.commands.CreateBlogPost;
import io.spine.examples.blog.commands.PublishBlogPost;
import io.spine.examples.blog.events.BlogPostCreated;
import io.spine.examples.blog.events.BlogPostPublished;
import io.spine.examples.blog.rejections.CannotPublishBlogPost;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * An aggregate that manages state of the {@link BlogPost} model.
 *
 * @author Anton Nikulin
 */
class BlogPostAggregate extends Aggregate<BlogPostId, BlogPost, BlogPostVBuilder> {

    @VisibleForTesting
    BlogPostAggregate(BlogPostId id) {
        super(id);
    }

    @Assign
    BlogPostCreated handle(CreateBlogPost cmd) {
        return BlogPostCreated.newBuilder()
                .setBlogPostId(cmd.getBlogPostId())
                .setTitle(cmd.getTitle())
                .setBody(cmd.getBody())
                .build();
    }

    @Assign
    BlogPostPublished handle(PublishBlogPost cmd) throws CannotPublishBlogPost {
        BlogPost post = getState();
        Status status = post.getStatus();
        BlogPostId postId = cmd.getBlogPostId();
        if (status != Status.DRAFT) {
            boolean published = status == Status.PUBLISHED;
            throw CannotPublishBlogPost
                    .newBuilder()
                    .setBlogPostId(postId)
                    .setAlreadyPublished(published)
                    .setAlreadyDeleted(!published)
                    .build();
        }
        return BlogPostPublished.newBuilder()
                .setBlogPostId(postId)
                .setTitle(post.getTitle())
                .setBody(post.getBody())
                .build();
    }

    @Apply
    void blogPostCreated(BlogPostCreated event) {
        getBuilder()
                .setId(event.getBlogPostId())
                .setTitle(event.getTitle())
                .setBody(event.getBody())
                .setStatus(Status.DRAFT);
    }

    @Apply
    void blogPostPublished(BlogPostPublished event) {
        getBuilder()
                .setId(event.getBlogPostId())
                .setStatus(Status.PUBLISHED);
    }
}
