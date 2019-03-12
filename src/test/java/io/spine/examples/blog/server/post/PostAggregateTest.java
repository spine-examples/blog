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

package io.spine.examples.blog.server.post;

import io.spine.base.CommandMessage;
import io.spine.server.type.CommandEnvelope;
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.Post;
import io.spine.examples.blog.PostId;
import io.spine.examples.blog.commands.CreatePost;
import io.spine.examples.blog.commands.PublishPost;
import io.spine.examples.blog.events.PostCreated;
import io.spine.examples.blog.events.PostPublished;
import io.spine.examples.blog.rejections.Rejections.CannotPublishPost;
import io.spine.server.entity.Repository;
import io.spine.testing.client.TestActorRequestFactory;
import io.spine.testing.server.aggregate.AggregateCommandTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.blog.Post.Status.DRAFT;
import static io.spine.examples.blog.Post.Status.PUBLISHED;
import static io.spine.examples.blog.given.TestIdentifiers.newBlogId;
import static io.spine.examples.blog.given.TestIdentifiers.newPostId;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("PostAggregate should")
class PostAggregateTest {

    private static final BlogId blogId = newBlogId();
    private static final PostId postId = newPostId(blogId);

    @Nested
    @DisplayName("handle CreatePost command")
    class CreatePostCommandTest extends PostAggregateCommandTest<CreatePost> {

        CreatePostCommandTest() {
            super(postId, CreatePost.newBuilder()
                                    .setPostId(postId)
                                    .setTitle("Test Post in a Test Blog")
                                    .build());
        }

        @Test
        @DisplayName("producing PostCreated event")
        void createPost() {
            PostId expectedPostId = entityId();
            expectThat(aggregate())
                    .producesEvent(PostCreated.class, event -> {
                        PostId newPostId = event.getPostId();
                        assertEquals(expectedPostId, newPostId);
                        assertEquals(blogId, newPostId.getBlogId());
                        assertEquals(message().getTitle(), event.getTitle());
                    });
        }

        @Test
        @DisplayName("changing the post state")
        @SuppressWarnings("CheckReturnValue") // Ignore result of the dispatched command.
        void changesState() {
            dispatchTo(aggregate());
            PostId postId = entityId();
            Post post = aggregate().state();
            assertEquals(postId, post.getId());
            assertEquals(message().getTitle(), post.getTitle());
            assertEquals(DRAFT, post.getStatus());
        }
    }

    @Nested
    @DisplayName("PublishPost should")
    class PublishPostCommandTest extends PostAggregateCommandTest<PublishPost> {

        private final TestActorRequestFactory requestFactory =
                new TestActorRequestFactory(getClass());

        PublishPostCommandTest() {
            super(postId, PublishPost.newBuilder()
                                     .setPostId(postId)
                                     .build());
        }

        @Override
        @BeforeEach
        public void setUp() {
            super.setUp();
            createPost();
        }

        private void createPost() {
            CreatePost command = CreatePost
                    .newBuilder()
                    .setPostId(entityId())
                    .setTitle("Test Blog Post")
                    .build();
            CommandEnvelope envelope = CommandEnvelope.of(requestFactory.createCommand(command));
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

            Post post = aggregate.state();

            assertEquals(postId, post.getId());
            assertEquals("Test Blog Post", post.getTitle());
            assertEquals(PUBLISHED, post.getStatus());
        }

        @Test
        @DisplayName("throw CannotPublishPost rejection when Post is already published")
        void publishPublishedPost() {
            // Publish the post.
            PostAggregate aggregate = aggregate();
            dispatchCommand(aggregate, createCommand());
            Post blog = aggregate.state();

            assertEquals(PUBLISHED, blog.getStatus());

            // Now try to publish again.
            expectThat(aggregate).throwsRejection(CannotPublishPost.class);
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
        protected Repository<PostId, PostAggregate> createRepository() {
            return new PostRepository();
        }

        protected final PostAggregate aggregate() {
            return aggregate;
        }
    }
}
