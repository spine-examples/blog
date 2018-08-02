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

import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.events.BlogCreated;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * An aggregate that manages state of the {@link Blog} model.
 *
 * @author Anton Nikulin
 */
public class BlogAggregate extends Aggregate<BlogId, Blog, BlogVBuilder> {

    @VisibleForTesting
    protected BlogAggregate(BlogId id) {
        super(id);
    }

    @Assign
    List<? extends Message> handle(CreateBlog cmd) {
        final BlogCreated result = BlogCreated.newBuilder()
                .setBlogId(cmd.getBlogId())
                .setName(cmd.getName())
                .build();
        return Collections.singletonList(result);
    }

    @Apply
    void blogCreated(BlogCreated event) {
        getBuilder()
                .setId(event.getBlogId())
                .setName(event.getName());
    }
}
