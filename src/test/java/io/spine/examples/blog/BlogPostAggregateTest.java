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

import com.google.protobuf.Message;
import io.spine.core.CommandEnvelope;
import io.spine.examples.blog.commands.CreateBlogPost;
import io.spine.examples.blog.commands.PublishBlogPost;
import io.spine.examples.blog.events.BlogPostCreated;
import io.spine.examples.blog.events.BlogPostPublished;
import io.spine.examples.blog.rejections.Rejections;
import io.spine.server.entity.Repository;
import io.spine.testing.client.TestActorRequestFactory;
import io.spine.testing.server.aggregate.AggregateCommandTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.blog.given.TestIdentifiers.newBlogId;
import static io.spine.examples.blog.given.TestIdentifiers.newBlogPostId;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("ClassCanBeStatic" /* JUnit nested classes cannot be static. */)
class BlogPostAggregateTest {
    @Nested
    class CreateBlogPostCommandTest extends BlogPostAggregateCommandTest<CreateBlogPost> {

        @Override
        protected CreateBlogPost createMessage() {
            return CreateBlogPost.newBuilder()
                    .setBlogPostId(id())
                    .setTitle("Test Post in a Test Blog")
                    .build();
        }

        @Test
        @DisplayName("CreateBlogPost should produce BlogPostCreated event and change BlogPost state")
        void createBlogPost() {
            this.expectThat(blogPostAggregate)
                    .producesEvent(BlogPostCreated.class, created -> {
                        assertEquals(id(), created.getBlogPostId());
                        assertEquals(blogId, created.getBlogPostId().getBlogId());
                        assertEquals(message().getTitle(), created.getTitle());
                    });

            final BlogPost aggregateState = blogPostAggregate.getState();
            assertEquals(id(), aggregateState.getId());
            assertEquals(message().getTitle(), aggregateState.getTitle());
            assertEquals(BlogPost.Status.DRAFT, aggregateState.getStatus());
        }
    }

    @Nested
    class PublishBlogPostCommandTest extends BlogPostAggregateCommandTest<PublishBlogPost> {

        @Override
        @BeforeEach
        protected void setUp() {
            super.setUp();
            TestActorRequestFactory requestFactory =
                    TestActorRequestFactory.newInstance(getClass());
            CreateBlogPost createBlogPost = CreateBlogPost.newBuilder()
                    .setBlogPostId(id())
                    .setTitle("Test Blog Post")
                    .build();
            dispatchCommand(blogPostAggregate, CommandEnvelope.of(requestFactory.command().create(createBlogPost)));
        }

        @Override
        protected PublishBlogPost createMessage() {
            return PublishBlogPost.newBuilder()
                    .setBlogPostId(id())
                    .build();
        }

        @Test
        @DisplayName("PublishBlogPost should produce BlogPostPublished event and change BlogPost status")
        void publishBlogPost() {
            this.expectThat(blogPostAggregate)
                    .producesEvent(BlogPostPublished.class, published -> assertEquals(id(), published.getBlogPostId()));

            final BlogPost aggregateState = blogPostAggregate.getState();
            assertEquals(id(), aggregateState.getId());
            assertEquals("Test Blog Post", aggregateState.getTitle());
            assertEquals(BlogPost.Status.PUBLISHED, aggregateState.getStatus());
        }

        @Test
        @DisplayName("PublishBlogPost should throw CannotPublishBlogPost rejection when BlogPost is already published")
        void publishPublishedBlogPost() {
            dispatchCommand(blogPostAggregate, CommandEnvelope.of(createCommand(message())));
            assertEquals(BlogPost.Status.PUBLISHED, blogPostAggregate.getState().getStatus());

            this.expectThat(blogPostAggregate)
                    .throwsRejection(Rejections.CannotPublishBlogPost.class);
        }
    }

    private static abstract class BlogPostAggregateCommandTest<C extends Message>
            extends AggregateCommandTest<BlogPostId, C, BlogPost, BlogPostAggregate> {

        final BlogId blogId = newBlogId();
        BlogPostAggregate blogPostAggregate;

        @Override
        @BeforeEach
        protected void setUp() {
            super.setUp();
            blogPostAggregate = new BlogPostAggregate(id());
        }

        @Override
        protected BlogPostId newId() {
            return newBlogPostId(blogId);
        }

        @Override
        protected Repository<BlogPostId, BlogPostAggregate> createEntityRepository() {
            return new BlogPostAggregateRepository();
        }
    }
}
