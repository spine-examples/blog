/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.examples.blog.web;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import io.spine.examples.blog.BlogContext;
import io.spine.io.Resource;
import io.spine.server.BoundedContext;
import io.spine.server.CommandService;
import io.spine.server.QueryService;
import io.spine.web.firebase.FirebaseClient;
import io.spine.web.firebase.FirebaseCredentials;

import java.io.IOException;
import java.io.InputStream;

import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static io.spine.web.firebase.FirebaseClientFactory.remoteClient;
import static io.spine.web.firebase.FirebaseCredentials.fromGoogleCredentials;

/**
 * The Blog application services.
 */
final class Application {

    private static final String FIREBASE_RDB = "https://spine-dev.firebaseio.com/";

    private static final Application instance = new Application();

    private final BoundedContext context;
    private final GoogleCredentials credentials;

    private Application() {
        context = BlogContext.builder().build();
        credentials = credentials();
        FirebaseOptions firebaseOptions = FirebaseOptions
                .builder()
                .setDatabaseUrl(FIREBASE_RDB)
                .setCredentials(credentials)
                .build();
        FirebaseApp.initializeApp(firebaseOptions);
    }

    /**
     * Obtains the singleton instance of {@code Application}.
     *
     * @apiNote This method is designed for static imports.
     */
    static Application app() {
        return instance;
    }

    /**
     * Constructs the {@link QueryService} for the Blog application.
     *
     * <p>The service contains the only context of the app — {@link BlogContext}.
     */
    QueryService queryService() {
        return QueryService
                .newBuilder()
                .add(context)
                .build();
    }

    /**
     * Constructs the {@link CommandService} for the Blog application.
     *
     * <p>The service contains the only context of the app — {@link BlogContext}.
     */
    CommandService commandService() {
        return CommandService
                .newBuilder()
                .add(context)
                .build();
    }

    /**
     * Constructs a {@link FirebaseClient} for client-server communication for the Blog application.
     *
     * <p>The {@link FirebaseClient} points to the {@code spine-dev} Firebase project.
     */
    FirebaseClient firebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseCredentials creds = fromGoogleCredentials(credentials);
        return remoteClient(database, creds);
    }

    private GoogleCredentials credentials() {
        Resource credentialFile = Resource.file(
                "spine-dev-firebase.json",
                Application.class.getClassLoader()
        );
        try (InputStream stream = credentialFile.open()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            return credentials;
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }
}
