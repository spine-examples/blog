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

import com.google.common.annotations.VisibleForTesting;
import io.spine.examples.blog.Blog;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.BlogVBuilder;
import io.spine.server.event.React;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.events.BlogCreated;
import io.spine.examples.blog.events.PostAdded;
import io.spine.examples.blog.events.PostCreated;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * An aggregate that manages state of the {@link io.spine.examples.blog.Blog} model.
 */
class BlogAggregate extends Aggregate<BlogId, Blog, BlogVBuilder> {

    @VisibleForTesting
    BlogAggregate(BlogId id) {
        super(id);
    }

    @Assign
    BlogCreated handle(CreateBlog cmd) {
        return BlogCreated.newBuilder()
                .setBlogId(cmd.getBlogId())
                .setTitle(cmd.getTitle())
                .build();
    }

    @React
    PostAdded on(PostCreated event) {
        return PostAdded.newBuilder()
                .setPostId(event.getPostId())
                .build();
    }

    @Apply
    void blogCreated(BlogCreated event) {
        getBuilder()
                .setId(event.getBlogId())
                .setTitle(event.getTitle());
    }

    @Apply
    void blogPostAdded(PostAdded event) {
        getBuilder()
                .setId(event.getPostId().getBlogId())
                .addPosts(event.getPostId());
    }
}
