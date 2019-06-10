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
import io.spine.examples.blog.BlogId;
import io.spine.examples.blog.Post;
import io.spine.examples.blog.PostId;
import io.spine.examples.blog.commands.CreatePost;
import io.spine.examples.blog.commands.PublishPost;
import io.spine.examples.blog.events.PostCreated;
import io.spine.examples.blog.events.PostPublished;
import io.spine.examples.blog.rejections.Rejections.CannotPublishPost;
import io.spine.server.DefaultRepository;
import io.spine.server.entity.Repository;
import io.spine.testing.client.TestActorRequestFactory;
import io.spine.testing.server.aggregate.AggregateCommandTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.examples.blog.Post.Status.DRAFT;
import static io.spine.examples.blog.Post.Status.PUBLISHED;
import static io.spine.examples.blog.given.TestIdentifiers.newPostId;
import static io.spine.testing.server.aggregate.AggregateMessageDispatcher.dispatchCommand;

@DisplayName("PostAggregate should")
class PostAggregateTest {

    private static final BlogId blogId = BlogId.generate();
    private static final PostId postId = newPostId(blogId);

    /**
     * Abstract base for tests of handling of Blog Post commands.
     *
     * <p>Nested classes below derive from this class, passing the command under the test as
     * a generic parameter.
     *
     * @param <C> the type of the blog post command message
     */
    private abstract static class PostCommandTest<C extends CommandMessage>
            extends AggregateCommandTest<PostId, C, Post, PostAggregate> {

        private PostAggregate aggregate;

        PostCommandTest(PostId aggregateId, C commandMessage) {
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
            return DefaultRepository.of(PostAggregate.class);
        }

        protected final PostAggregate aggregate() {
            return aggregate;
        }
    }

    @Nested
    @DisplayName("handle CreatePost command")
    class CreatePostCommandTest extends PostCommandTest<CreatePost> {

        CreatePostCommandTest() {
            super(postId,
                  CreatePost
                          .newBuilder()
                          .setPostId(postId)
                          .setTitle("Test Post in a Test Blog")
                          .build()
            );
        }

        @Test
        @DisplayName("producing PostCreated event")
        void createPost() {
            PostId expectedPostId = entityId();
            expectThat(aggregate())
                    .producesEvent(PostCreated.class, event -> {
                        PostId newPostId = event.getPostId();

                        assertThat(newPostId)
                                .isEqualTo(expectedPostId);
                        assertThat(newPostId.getBlogId())
                                .isEqualTo(blogId);
                        assertThat(event.getTitle())
                                .isEqualTo(message().getTitle());
                    });
        }

        @Test
        @DisplayName("changing the post state")
        @SuppressWarnings("CheckReturnValue") // Ignore result of the dispatched command.
        void changesState() {
            dispatchTo(aggregate());
            PostId postId = entityId();
            Post post = aggregate().state();

            assertThat(post.getId())
                    .isEqualTo(postId);
            assertThat(post.getTitle())
                    .isEqualTo(message().getTitle());
            assertThat(post.getStatus())
                    .isEqualTo(DRAFT);
        }
    }

    @Nested
    @DisplayName("PublishPost should")
    class PublishPostCommandTest extends PostCommandTest<PublishPost> {

        private static final String POST_TITLE = "Test Blog Post";

        private final TestActorRequestFactory requestFactory =
                new TestActorRequestFactory(getClass());

        PublishPostCommandTest() {
            super(postId,
                  PublishPost
                          .newBuilder()
                          .setPostId(postId)
                          .build()
            );
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
                    .setTitle(POST_TITLE)
                    .build();
            dispatchCommand(aggregate(), requestFactory.createCommand(command));
        }

        @Test
        @DisplayName("produce `PostPublished` event and change `Post` status")
        void publishPost() {
            PostId postId = entityId();
            PostAggregate aggregate = aggregate();
            expectThat(aggregate)
                    .producesEvent(PostPublished.class, event ->
                            assertThat(event.getPostId())
                                    .isEqualTo(postId)
                    );

            Post post = aggregate.state();
            assertThat(post.getId())
                    .isEqualTo(postId);
            assertThat(post.getTitle())
                    .isEqualTo(POST_TITLE);
            assertThat(post.getStatus())
                    .isEqualTo(PUBLISHED);
        }

        @Test
        @DisplayName("throw `CannotPublishPost` rejection when `Post` is already published")
        void publishPublishedPost() {
            // Publish the post.
            PostAggregate aggregate = aggregate();
            dispatchCommand(aggregate, createCommand());

            Post blog = aggregate.state();
            assertThat(blog.getStatus())
                    .isEqualTo(PUBLISHED);

            // Now try to publish again.
            expectThat(aggregate).throwsRejection(CannotPublishPost.class);
        }
    }
}
