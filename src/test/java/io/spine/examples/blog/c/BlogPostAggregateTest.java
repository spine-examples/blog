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
import io.spine.core.CommandEnvelope;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.BlogPost;
import io.spine.examples.blog.BlogPostId;
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
@DisplayName("BlogPostAggregate should")
class BlogPostAggregateTest {

    private static final BlogId blogId = newBlogId();
    private static final BlogPostId blogPostId = newBlogPostId(blogId);

    @Nested
    @DisplayName("handle CreateBlogPost command")
    class CreateBlogPostCommandTest extends BlogPostAggregateCommandTest<CreateBlogPost> {

        CreateBlogPostCommandTest() {
            super(blogPostId, CreateBlogPost.newBuilder()
                                            .setBlogPostId(blogPostId)
                                            .setTitle("Test Post in a Test Blog")
                                            .build());
        }

        /**
         * Dispatches the command to the aggregate.
         */
        @Override
        @BeforeEach
        public void setUp() {
            super.setUp();
            dispatchTo(aggregate());
        }

        @Test
        @DisplayName("producing BlogPostCreated event")
        void createBlogPost() {
            BlogPostId expectedPostId = entityId();
            this.expectThat(aggregate())
                .producesEvent(BlogPostCreated.class, event -> {
                    BlogPostId newPostId = event.getBlogPostId();
                    assertEquals(expectedPostId, newPostId);
                        assertEquals(blogId, newPostId.getBlogId());
                        assertEquals(message().getTitle(), event.getTitle());
                    });
        }

        @Test
        @DisplayName("changing the post state")
        void changesState() {
            BlogPostId postId = entityId();
            BlogPost post = aggregate().getState();
            assertEquals(postId, post.getId());
            assertEquals(message().getTitle(), post.getTitle());
            assertEquals(BlogPost.Status.DRAFT, post.getStatus());
        }
    }

    @Nested
    @DisplayName("PublishBlogPost should")
    class PublishBlogPostCommandTest extends BlogPostAggregateCommandTest<PublishBlogPost> {

        PublishBlogPostCommandTest() {
            super(blogPostId, PublishBlogPost.newBuilder()
                                             .setBlogPostId(blogPostId)
                                             .build());
        }

        @Override
        @BeforeEach
        public void setUp() {
            super.setUp();
            TestActorRequestFactory requestFactory =
                    TestActorRequestFactory.newInstance(getClass());
            CreateBlogPost command = CreateBlogPost
                    .newBuilder()
                    .setBlogPostId(entityId())
                    .setTitle("Test Blog Post")
                    .build();
            CommandEnvelope envelope = requestFactory.createEnvelope(command);
            dispatchCommand(aggregate(), envelope);
        }

        @Test
        @DisplayName("produce BlogPostPublished event and change BlogPost status")
        void publishBlogPost() {
            BlogPostId postId = entityId();
            BlogPostAggregate aggregate = aggregate();
            expectThat(aggregate)
                .producesEvent(BlogPostPublished.class,
                               published -> assertEquals(postId, published.getBlogPostId()));

            BlogPost blogPost = aggregate.getState();

            assertEquals(postId, blogPost.getId());
            assertEquals("Test Blog Post", blogPost.getTitle());
            assertEquals(BlogPost.Status.PUBLISHED, blogPost.getStatus());
        }

        @Test
        @DisplayName("throw CannotPublishBlogPost rejection when BlogPost is already published")
        void publishPublishedBlogPost() {
            BlogPostAggregate aggregate = aggregate();
            dispatchCommand(aggregate, createCommand());
            BlogPost blog = aggregate.getState();

            assertEquals(BlogPost.Status.PUBLISHED, blog.getStatus());

            expectThat(aggregate).throwsRejection(Rejections.CannotPublishBlogPost.class);
        }
    }

    /**
     * Abstract base for tests of handling of Blog Post commands.
     *
     * @param <C> the type of the blog post command message
     */
    private abstract static class BlogPostAggregateCommandTest<C extends CommandMessage>
            extends AggregateCommandTest<BlogPostId, C, BlogPost, BlogPostAggregate> {

        private BlogPostAggregate aggregate;

        BlogPostAggregateCommandTest(BlogPostId aggregateId, C commandMessage) {
            super(aggregateId, commandMessage);
        }

        /**
         * Creates the aggregate under the tests.
         */
        @Override
        @BeforeEach
        public void setUp() {
            super.setUp();
            aggregate = new BlogPostAggregate(entityId());
        }

        @Override
        protected Repository<BlogPostId, BlogPostAggregate> createEntityRepository() {
            return new BlogPostRepository();
        }

        protected final BlogPostAggregate aggregate() {
            return aggregate;
        }
    }
}
