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

package io.spine.examples.blog.server.blog;

import io.spine.base.CommandMessage;
import io.spine.examples.blog.Blog;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.events.BlogCreated;
import io.spine.server.entity.Repository;
import io.spine.testing.server.aggregate.AggregateCommandTest;
import io.spine.testing.server.expected.CommandHandlerExpected;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("BlogAggregate should")
class BlogAggregateTest {

    private static final BlogId blogId = BlogId.generate();
    private static final CreateBlog createCommand = CreateBlog.newBuilder()
                                                              .setTitle("DDD in Pictures")
                                                              .setId(blogId)
                                                              .vBuild();
    @Nested
    @DisplayName("handle CreateBlog command")
    class CreateBlogCommandTest extends BlogAggregateCommandTest<CreateBlog> {

        private BlogId expectedId;
        private Blog blog;
        private String expectedTitle;

        CreateBlogCommandTest() {
            super(blogId, createCommand);
        }

        @Override
        @BeforeEach
        public void setUp() {
            super.setUp();
            expectedId = entityId();
            expectedTitle = message().getTitle();
            blog = aggregate().state();
        }

        @Test
        @DisplayName("producing BlogCreated event")
        void produceEvent() {
            expectThat()
                    .producesEvent(BlogCreated.class, created -> {
                        assertEquals(expectedId, created.getId());
                        assertEquals(expectedTitle, created.getTitle());
                    });
        }

        @Test
        @DisplayName("setting ID")
        void setId() {
            assertEquals(expectedId, blog.getId());
        }

        @Test
        @DisplayName("setting title")
        void setTitle() {
            assertEquals(expectedTitle, blog.getTitle());
        }
    }

    private abstract static class BlogAggregateCommandTest<C extends CommandMessage>
            extends AggregateCommandTest<BlogId, C, Blog, BlogAggregate> {

        private BlogAggregate aggregate;
        private CommandHandlerExpected<Blog> expectThat;

        BlogAggregateCommandTest(BlogId aggregateId, C commandMessage) {
            super(aggregateId, commandMessage);
        }

        protected final BlogAggregate aggregate() {
            return aggregate;
        }

        @Override
        @BeforeEach
        public void setUp() {
            super.setUp();
            aggregate = new BlogAggregate(entityId());
            expectThat = expectThat(aggregate);
        }

        final CommandHandlerExpected<Blog> expectThat() {
            return expectThat;
        }

        @Override
        protected Repository<BlogId, BlogAggregate> createRepository() {
            return new BlogRepository();
        }
    }
}
