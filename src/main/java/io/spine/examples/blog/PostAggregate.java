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

import java.util.Collections;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;

import io.spine.examples.blog.commands.CreatePost;
import io.spine.examples.blog.events.PostCreated;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * An aggregate that manages state of the {@link Post} model.
 *
 * @author Anton Nikulin
 */
public class PostAggregate extends Aggregate<PostId, Post, PostVBuilder> {

    @VisibleForTesting
    protected PostAggregate(PostId id) {
        super(id);
    }

    @Assign
    List<? extends Message> handle(CreatePost cmd) {
        final PostCreated result = PostCreated.newBuilder()
                .setPostId(cmd.getPostId())
                .setBlogId(cmd.getBlogId())
                .setTitle(cmd.getTitle())
                .setBody(cmd.getBody())
                .build();
        return Collections.singletonList(result);
    }

    @Apply
    void postCreated(PostCreated event) {
        getBuilder()
                .setId(event.getPostId())
                .setTitle(event.getTitle())
                .setBody(event.getBody())
                .setStatus(Post.Status.DRAFT);
    }

}
