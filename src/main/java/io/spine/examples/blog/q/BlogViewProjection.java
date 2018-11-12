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

package io.spine.examples.blog.q;

import io.spine.core.Subscribe;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.BlogPostId;
import io.spine.examples.blog.BlogPostItem;
import io.spine.examples.blog.BlogView;
import io.spine.examples.blog.BlogViewVBuilder;
import io.spine.examples.blog.events.BlogPostPublished;
import io.spine.server.projection.Projection;

/**
 * A projection that represents the current state of a blog and contains published blog posts.
 */
class BlogViewProjection extends Projection<BlogId, BlogView, BlogViewVBuilder> {

    BlogViewProjection(BlogId id) {
        super(id);
    }

    @Subscribe
    public void on(BlogPostPublished event) {
        BlogPostId postId = event.getBlogPostId();
        BlogPostItem item = BlogPostItem
                .newBuilder()
                .setId(postId)
                .setBody(event.getBody())
                .setTitle(event.getTitle())
                .build();
        getBuilder()
                .setBlogId(postId.getBlogId())
                .addPosts(item);
    }
}
