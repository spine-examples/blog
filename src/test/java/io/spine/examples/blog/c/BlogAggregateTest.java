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

package io.spine.examples.blog.c;

import io.spine.base.CommandMessage;
import io.spine.examples.blog.Blog;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.events.BlogCreated;
import io.spine.server.entity.Repository;
import io.spine.testing.server.aggregate.AggregateCommandTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.blog.given.TestIdentifiers.newBlogId;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("ClassCanBeStatic" /* JUnit nested classes cannot be static. */)
class BlogAggregateTest {

    private static final BlogId blogId = newBlogId();
    private static final CreateBlog createCommand = CreateBlog.newBuilder()
                                                              .setName("Test Blog Name")
                                                              .setBlogId(blogId)
                                                              .build();
    @Nested
    class CreateBlogCommandTest extends BlogAggregateCommandTest<CreateBlog> {

        CreateBlogCommandTest() {
            super(blogId, createCommand);
        }

        @Test
        @DisplayName("CreateBlog should produce BlogCreated event and change Blog state")
        void test() {
            BlogId blogId = entityId();
            String expectedBlogName = message().getName();
            this.expectThat(blogAggregate)
                .producesEvent(BlogCreated.class, created -> {
                        assertEquals(blogId, created.getBlogId());
                        assertEquals(expectedBlogName, created.getName());
                    });

            Blog blog = blogAggregate.getState();
            assertEquals(blogId, blog.getId());
            assertEquals(expectedBlogName, blog.getName());
        }
    }

    private static abstract class BlogAggregateCommandTest<C extends CommandMessage>
            extends AggregateCommandTest<BlogId, C, Blog, BlogAggregate> {

        BlogAggregate blogAggregate;

        BlogAggregateCommandTest(BlogId aggregateId, C commandMessage) {
            super(aggregateId, commandMessage);
        }

        @Override
        @BeforeEach
        public void setUp() {
            super.setUp();
            blogAggregate = new BlogAggregate(entityId());
        }

        @Override
        protected Repository<BlogId, BlogAggregate> createEntityRepository() {
            return new BlogRepository();
        }
    }
}
