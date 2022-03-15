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

package io.spine.examples.blog.server.blog;

import io.spine.examples.blog.Blog;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.events.BlogCreated;
import io.spine.examples.blog.events.PostAdded;
import io.spine.examples.blog.events.PostCreated;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;
import io.spine.server.event.React;

/**
 * An aggregate that manages state of the {@link Blog} model.
 */
final class BlogAggregate extends Aggregate<BlogId, Blog, Blog.Builder> {

    BlogAggregate(BlogId id) {
        super(id);
    }

    @Assign
    BlogCreated handle(CreateBlog c) {
        return BlogCreated
                .newBuilder()
                .setId(c.getId())
                .setTitle(c.getTitle())
                .vBuild();
    }

    /**
     * Whenever a new post is created, it is automatically added to the blog.
     */
    @React
    PostAdded postCreationPolicy(PostCreated e) {
        return PostAdded
                .newBuilder()
                .setPost(e.getId())
                .setBlog(e.getBlog())
                .vBuild();
    }

    @Apply
    private void event(BlogCreated e) {
        builder().setTitle(e.getTitle());
    }

    @Apply
    private void event(PostAdded e) {
        builder().addPost(e.getPost());
    }
}
