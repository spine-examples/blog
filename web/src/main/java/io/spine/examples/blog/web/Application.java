/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
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
import io.spine.web.firebase.rest.RemoteDatabaseClient;

import java.io.IOException;
import java.io.InputStream;

import static io.spine.util.Exceptions.illegalStateWithCauseOf;

public final class Application {

    private static final String FIREBASE_RDB = "https://spine-dev.firebaseio.com/";

    private static final BoundedContext context = BlogContext.builder().build();

    static {
        FirebaseOptions firebaseOptions = FirebaseOptions
                .builder()
                .setDatabaseUrl(FIREBASE_RDB)
                .setCredentials(credentials())
                .build();
        FirebaseApp.initializeApp(firebaseOptions);
    }

    public static QueryService queryService() {
        return QueryService
                .newBuilder()
                .add(context)
                .build();
    }

    public static CommandService commandService() {
        return CommandService
                .newBuilder()
                .add(context)
                .build();
    }

    public static FirebaseClient firebase() {
        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        return RemoteDatabaseClient
                .newBuilder()
                .setRequestFactory(requestFactory)
                .setDatabase(instance)
                .build();
    }

    private static GoogleCredentials credentials() {
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
