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

import io.spine.core.Subscribe;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.BlogView;
import io.spine.examples.blog.PostItem;
import io.spine.examples.blog.events.BlogCreated;
import io.spine.examples.blog.events.PostPublished;
import io.spine.server.projection.Projection;

/**
 * A projection that represents the current state of a blog and contains published blog posts.
 */
final class BlogViewProjection extends Projection<BlogId, BlogView, BlogView.Builder> {

    BlogViewProjection(BlogId id) {
        super(id);
    }

    @Subscribe
    void on(BlogCreated event) {
        builder().setTitle(event.getTitle());
    }

    @Subscribe
    void on(PostPublished event) {
        PostItem item = toPostItem(event);
        builder().addPost(0, item);
    }

    /** Converts publishing event to a post item. */
    private static PostItem toPostItem(PostPublished event) {
        return PostItem
                .newBuilder()
                .setId(event.getPost())
                .setTitle(event.getTitle())
                .setBody(event.getBody())
                .vBuild();
    }
}
