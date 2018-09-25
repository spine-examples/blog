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

import io.spine.core.BoundedContextName;
import io.spine.server.BoundedContext;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.memory.InMemoryStorageFactory;

/**
 * A {@link BoundedContext} for the Blog application using {@link InMemoryStorageFactory} for storage.
 *
 * @author Anton Nikulin
 */
public class BlogBoundedContext {

    private static final String DEFAULT_BOUNDED_CONTEXT_NAME = "Blog";

    private BlogBoundedContext() {
    }

    public static BoundedContext getInstance() {
        return getInstance(DEFAULT_BOUNDED_CONTEXT_NAME);
    }

    public static BoundedContext getInstance(String contextName) {
        final BoundedContextName name = BoundedContextName.newBuilder().setValue(contextName).build();
        final StorageFactory storageFactory = InMemoryStorageFactory.newInstance(name, false);

        final BoundedContext context = BoundedContext
                .newBuilder()
                .setName(name)
                .setStorageFactorySupplier(() -> storageFactory)
                .build();

        context.register(new BlogAggregateRepository());
        context.register(new BlogPostAggregateRepository());
        context.register(new BlogViewRepository());
        return context;
    }
}
