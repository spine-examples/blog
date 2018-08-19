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

import com.google.protobuf.Message;
import io.spine.examples.blog.commands.CreateBlogPost;
import io.spine.examples.blog.events.BlogPostCreated;
import io.spine.server.entity.Repository;
import io.spine.testing.server.aggregate.AggregateCommandTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.blog.given.TestIdentifiers.newBlogId;
import static io.spine.examples.blog.given.TestIdentifiers.newBlogPostId;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreatePostCommandTest extends AggregateCommandTest<BlogPostId, CreateBlogPost, BlogPost, BlogPostAggregate> {

    private final BlogId blogId = newBlogId();

    @Override
    protected BlogPostId newId() {
        return newBlogPostId();
    }

    @Override
    protected CreateBlogPost createMessage() {
        return CreateBlogPost.newBuilder()
                .setBlogPostId(id())
                .setBlogId(blogId)
                .setTitle("Test Post in a Test Blog")
                .build();
    }

    @Override
    protected Repository<BlogPostId, BlogPostAggregate> createEntityRepository() {
        return new BlogPostAggregateRepository();
    }

    @Test
    void testCreatePostShouldProduceEvent() {
        final BlogPostAggregate blogPostAggregate = new BlogPostAggregate(id());

        this.expectThat(blogPostAggregate)
                .producesEvent(BlogPostCreated.class, created -> {
                    assertEquals(id(), created.getBlogPostId());
                    assertEquals(blogId, created.getBlogId());
                    assertEquals(message().getTitle(), created.getTitle());
                });
    }

    @Test
    void testCreatePostShouldChangePostState() {
        final BlogPostAggregate blogPostAggregate = new BlogPostAggregate(id());
        final List<? extends Message> messages = dispatchTo(blogPostAggregate);

        final BlogPost aggregateState = blogPostAggregate.getState();
        assertEquals(id(), aggregateState.getId());
        assertEquals(message().getTitle(), aggregateState.getTitle());
        assertEquals(BlogPost.Status.DRAFT, aggregateState.getStatus());
    }
}
