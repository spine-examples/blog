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
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.events.BlogCreated;
import io.spine.server.entity.Repository;
import io.spine.testing.server.aggregate.AggregateCommandTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.blog.given.TestIdentifiers.newBlogId;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateBlogCommandTest extends AggregateCommandTest<BlogId, CreateBlog, Blog, BlogAggregate> {

    @Override
    protected BlogId newId() {
        return newBlogId();
    }

    @Override
    protected CreateBlog createMessage() {
        return CreateBlog.newBuilder()
                .setBlogId(id())
                .setName("TestBlog")
                .build();
    }

    @Override
    protected Repository<BlogId, BlogAggregate> createEntityRepository() {
        return new BlogAggregateRepository();
    }

    @Test
    void testCreateBlogShouldProduceEvent() {
        final BlogAggregate blogAggregate = new BlogAggregate(id());

        this.expectThat(blogAggregate)
                .producesEvent(BlogCreated.class, created -> {
                    assertEquals(id(), created.getBlogId());
                    assertEquals(message().getName(), created.getName());
                });

    }

    @Test
    void testCreateBlogShouldChangeBlogState() {
        final BlogAggregate blogAggregate = new BlogAggregate(id());
        final List<? extends Message> messages = dispatchTo(blogAggregate);

        assertEquals(id(), blogAggregate.getState().getId());
        assertEquals(message().getName(), blogAggregate.getState().getName());
    }
}
