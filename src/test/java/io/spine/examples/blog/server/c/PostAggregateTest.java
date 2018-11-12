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

package io.spine.examples.blog.server.c;

import io.spine.base.CommandMessage;
import io.spine.core.CommandEnvelope;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.Post;
import io.spine.examples.blog.PostId;
import io.spine.examples.blog.commands.CreatePost;
import io.spine.examples.blog.commands.PublishPost;
import io.spine.examples.blog.events.PostCreated;
import io.spine.examples.blog.events.PostPublished;
import io.spine.examples.blog.rejections.Rejections;
import io.spine.server.entity.Repository;
import io.spine.testing.client.TestActorRequestFactory;
import io.spine.testing.server.aggregate.AggregateCommandTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.blog.given.TestIdentifiers.newBlogId;
import static io.spine.examples.blog.given.TestIdentifiers.newPostId;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("PostAggregate should")
class PostAggregateTest {

    private static final BlogId blogId = newBlogId();
    private static final PostId blogPostId = newPostId(blogId);

    @Nested
    @DisplayName("handle CreatePost command")
    class CreatePostCommandTest extends PostAggregateCommandTest<CreatePost> {

        CreatePostCommandTest() {
            super(blogPostId, CreatePost.newBuilder()
                                            .setPostId(blogPostId)
                                            .setTitle("Test Post in a Test Blog")
                                            .build());
        }

        /**
         * Dispatches the command to the aggregate.
         */
        @Override
        @BeforeEach
        @SuppressWarnings("CheckReturnValue") // We can ignore result of dispatched command.
        public void setUp() {
            super.setUp();
            dispatchTo(aggregate());
        }

        @Test
        @DisplayName("producing PostCreated event")
        void createPost() {
            PostId expectedPostId = entityId();
            this.expectThat(aggregate())
                .producesEvent(PostCreated.class, event -> {
                    PostId newPostId = event.getPostId();
                    assertEquals(expectedPostId, newPostId);
                        assertEquals(blogId, newPostId.getBlogId());
                        assertEquals(message().getTitle(), event.getTitle());
                    });
        }

        @Test
        @DisplayName("changing the post state")
        void changesState() {
            PostId postId = entityId();
            Post post = aggregate().getState();
            assertEquals(postId, post.getId());
            assertEquals(message().getTitle(), post.getTitle());
            assertEquals(Post.Status.DRAFT, post.getStatus());
        }
    }

    @Nested
    @DisplayName("PublishPost should")
    class PublishPostCommandTest extends PostAggregateCommandTest<PublishPost> {

        PublishPostCommandTest() {
            super(blogPostId, PublishPost.newBuilder()
                                             .setPostId(blogPostId)
                                             .build());
        }

        @Override
        @BeforeEach
        public void setUp() {
            super.setUp();
            TestActorRequestFactory requestFactory =
                    TestActorRequestFactory.newInstance(getClass());
            CreatePost command = CreatePost
                    .newBuilder()
                    .setPostId(entityId())
                    .setTitle("Test Blog Post")
                    .build();
            CommandEnvelope envelope = requestFactory.createEnvelope(command);
            dispatchCommand(aggregate(), envelope);
        }

        @Test
        @DisplayName("produce PostPublished event and change Post status")
        void publishPost() {
            PostId postId = entityId();
            PostAggregate aggregate = aggregate();
            expectThat(aggregate)
                .producesEvent(PostPublished.class,
                               published -> assertEquals(postId, published.getPostId()));

            Post blogPost = aggregate.getState();

            assertEquals(postId, blogPost.getId());
            assertEquals("Test Blog Post", blogPost.getTitle());
            assertEquals(Post.Status.PUBLISHED, blogPost.getStatus());
        }

        @Test
        @DisplayName("throw CannotPublishPost rejection when Post is already published")
        void publishPublishedPost() {
            PostAggregate aggregate = aggregate();
            dispatchCommand(aggregate, createCommand());
            Post blog = aggregate.getState();

            assertEquals(Post.Status.PUBLISHED, blog.getStatus());

            expectThat(aggregate).throwsRejection(Rejections.CannotPublishPost.class);
        }
    }

    /**
     * Abstract base for tests of handling of Blog Post commands.
     *
     * @param <C> the type of the blog post command message
     */
    private abstract static class PostAggregateCommandTest<C extends CommandMessage>
            extends AggregateCommandTest<PostId, C, Post, PostAggregate> {

        private PostAggregate aggregate;

        PostAggregateCommandTest(PostId aggregateId, C commandMessage) {
            super(aggregateId, commandMessage);
        }

        /**
         * Creates the aggregate under the tests.
         */
        @Override
        @BeforeEach
        public void setUp() {
            super.setUp();
            aggregate = new PostAggregate(entityId());
        }

        @Override
        protected Repository<PostId, PostAggregate> createEntityRepository() {
            return new PostRepository();
        }

        protected final PostAggregate aggregate() {
            return aggregate;
        }
    }
}
