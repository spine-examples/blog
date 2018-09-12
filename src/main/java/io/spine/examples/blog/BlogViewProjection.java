/*
 * Copyright 2018, TeamDev. All rights reserved.
 *
 *  Redistribution and use in source and/or binary forms, with or without
 *  modification, must retain the above copyright notice and the following
 *  disclaimer.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.examples.blog;

import io.spine.core.EventContext;
import io.spine.core.Subscribe;
import io.spine.examples.blog.events.BlogPostPublished;
import io.spine.server.projection.Projection;

import java.util.Optional;

import static io.spine.core.Enrichments.getEnrichment;

/**
 * A projection that represents the current state of a blog and contains published blog posts.
 *
 * @author Anton Nikulin
 */
public class BlogViewProjection extends Projection<BlogId, BlogView, BlogViewVBuilder> {

    protected BlogViewProjection(BlogId id) {
        super(id);
    }

    @Subscribe
    public void on(BlogPostPublished event, EventContext context) {
        final Optional<BlogPostEnrichment> enrichment = getEnrichment(BlogPostEnrichment.class, context);
        enrichment.ifPresent((enr) -> {
            final BlogPost blogPost = enr.getBlogPost();
            final BlogPostItem item = BlogPostItem.newBuilder()
                    .setId(blogPost.getId())
                    .setBody(blogPost.getBody())
                    .setTitle(blogPost.getTitle())
                    .build();
            getBuilder()
                    .setBlogId(blogPost.getBlogId())
                    .addPosts(item);
        });
    }
}
