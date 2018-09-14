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

import io.spine.server.event.EventEnricher;

import java.util.Optional;

/**
 * An {@link EventEnricher} factory for the blog bounded context.
 *
 * @author Anton Nikulin
 */
public class BlogEnrichments {
    private BlogPostAggregateRepository blogPostRepo;

    private BlogEnrichments(Builder builder) {
        this.blogPostRepo = builder.blogPostRepo;
    }

    public EventEnricher createEventEnricher() {
        return EventEnricher.newBuilder()
                .add(BlogPostId.class, BlogPost.class, this::blogPostIdToBlogPost)
                .build();
    }

    private BlogPost blogPostIdToBlogPost(BlogPostId id) {
        Optional<BlogPostAggregate> blogPostAggregate = blogPostRepo.find(id);
        return blogPostAggregate.isPresent() ? blogPostAggregate.get().getState() : BlogPost.getDefaultInstance();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    static class Builder {

        private BlogPostAggregateRepository blogPostRepo;

        /**
         * The {@code private} constructor prevents direct instantiation.
         */
        private Builder() {
        }

        public Builder setBlogPostRepository(BlogPostAggregateRepository blogPostRepo) {
            this.blogPostRepo = blogPostRepo;
            return this;
        }

        public BlogEnrichments build() {
            return new BlogEnrichments(this);
        }
    }
}
