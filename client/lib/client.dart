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

import 'dart:math';

import 'package:firebase/firebase.dart' as fb;
import 'package:protobuf/protobuf.dart';
import 'package:spine_client/spine_client.dart' as spine;
import 'package:spine_client/web_firebase_client.dart';

import 'blog/blog.pb.dart';
import 'blog/identifiers.pb.dart';
import 'types.dart' as blogTypes;

/// A client which executes queries and sends commands.
abstract class Client {

    /// Fetches all the `Blog`s.
    Stream<Blog> fetchBlogs();

    /// Fetches a single `Blog` with all of its `Post`s.
    Future<BlogView> fetchBlogWithPosts(BlogId id);

    /// Posts the given command message.
    Future<void> post(GeneratedMessage command);

    /// Posts the given command message and subscribes to the first event of type `E` which is
    /// emitted as the result of the command.
    Future<E> observeAfterCommand<E extends GeneratedMessage>(GeneratedMessage command);
}

/// A client which sends queries and commands over the network to a real server.
class NetworkClient extends Client {

    static final fb.App _firebaseApp = fb.initializeApp(
        apiKey: "AIzaSyD8Nr2zrW9QFLbNS5Kg-Ank-QIZP_jo5pU",
        authDomain: "spine-dev.firebaseapp.com",
        databaseURL: "https://spine-dev.firebaseio.com",
        projectId: "spine-dev",
        storageBucket: "",
        messagingSenderId: "165066236051"
    );

    final spine.Client _client;

    /// Creates a new `NetworkClient` with the given server and Firebase.
    ///
    NetworkClient(String serverUrl)
        : _client = spine.Clients(serverUrl,
                                  firebase: WebFirebaseClient(_firebaseApp.database()),
                                  guestId: spine.UserId()..value = 'Example Dart client',
                                  typeRegistries: [blogTypes.types()])
                         .asGuest();

    @override
    Stream<Blog> fetchBlogs() => _client.select<Blog>().post();

    @override
    Future<BlogView> fetchBlogWithPosts(BlogId id) =>
        _client.select<BlogView>().whereIds([id]).post().first;

    @override
    Future<void> post(GeneratedMessage command) => _client.command(command).postAndForget();

    @override
    Future<E> observeAfterCommand<E extends GeneratedMessage>(GeneratedMessage command) {
        var request = _client.command(command);
        var events = request.observeEvents<E>();
        request.post();
        return events.first
                     .catchError((e) => _onError(e, command));
    }

    void _onError(dynamic error, GeneratedMessage command) {
        print('Error when posting command `${command.runtimeType}`: $error');
    }
}

/// A client which generates pre-set data in response to queries and silently
/// "swallows" commands.
///
class FakeClient extends Client {

    final Random _rand = Random();

    @override
    Future<BlogView> fetchBlogWithPosts(BlogId id) async {
        return BlogView()
            ..id = id
            ..title = 'Blog ${id.uuid}'
            ..post.addAll([
                PostItem()
                    ..id = (PostId()..uuid = '${id.uuid}-1')
                    ..title = 'Blog entry ${_rand.nextInt(10)}'
                    ..body = 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean ut '
                        'mollis dui. Pellentesque ac vehicula eros. Mauris eu pulvinar nibh, ut '
                        'mollis elit. Pellentesque blandit tristique magna at volutpat. Quisque '
                        'velit lectus, auctor in rutrum sit amet, rutrum posuere enim. Aenean '
                        'faucibus, nisl eget iaculis mattis, metus enim efficitur nibh, posuere '
                        'maximus dui augue a lectus. Sed sagittis eu lacus a pulvinar. Aenean '
                        'felis neque, venenatis a maximus sed, tempus in sem. Vestibulum facilisis '
                        'orci quis nulla vulputate venenatis vulputate ut tellus. Ut aliquam '
                        'a ipsum tempor, et dapibus justo consequat. Nullam nec tempus felis. Orci '
                        'varius natoque penatibus et magnis dis parturient montes, nascetur '
                        'ridiculus mus.',
                PostItem()
                    ..id = (PostId()..uuid = '${id.uuid}-2')
                    ..title = 'Blog entry ${_rand.nextInt(10)}'
                    ..body = 'Phasellus sapien augue, sagittis ac lectus vitae, venenatis tempus '
                        'dui. Donec et libero sed eros vulputate facilisis in quis nulla. Quisque '
                        'eget nisi mollis, rutrum nisl sit amet, sollicitudin eros. Pellentesque '
                        'pellentesque rutrum dui vel scelerisque. Suspendisse nisl eros, placerat '
                        'sit amet nisl sit amet, efficitur mollis arcu. Nunc metus arcu, imperdiet '
                        'ut viverra eu, iaculis id orci. Donec fringilla at justo sed luctus. '
                        'Donec maximus ullamcorper vestibulum. Maecenas condimentum nibh orci, '
                        'quis cursus enim maximus id. Proin volutpat lectus non tellus malesuada '
                        'maximus. Morbi nisl magna, aliquet sed sollicitudin vel, consequat a mi. '
                        'Phasellus id dolor enim. Class aptent taciti sociosqu ad litora torquent '
                        'per conubia nostra, per inceptos himenaeos.',
            ]);
    }

    @override
    Stream<Blog> fetchBlogs() {
        var blogIdA = BlogId()..uuid = _rand.nextInt(314).toString();
        var blogIdB = BlogId()..uuid = _rand.nextInt(314).toString();
        var blogIdC = BlogId()..uuid = _rand.nextInt(314).toString();
        return Stream.fromIterable([
            Blog()
                ..id = blogIdA
                ..title = 'Blog ${blogIdA.uuid}'
                ..post.addAll([
                    PostId()..uuid = '${blogIdA.uuid}-1',
                    PostId()..uuid = '${blogIdA.uuid}-2'
                ]),
            Blog()
                ..id = blogIdB
                ..title = 'Blog ${blogIdB.uuid}'
                ..post.addAll([
                    PostId()..uuid = '${blogIdB.uuid}-1',
                    PostId()..uuid = '${blogIdB.uuid}-2'
                ]),
            Blog()
                ..id = blogIdC
                ..title = 'Blog ${blogIdC.uuid}'
                ..post.addAll([
                    PostId()..uuid = '${blogIdC.uuid}-1',
                    PostId()..uuid = '${blogIdC.uuid}-2'
                ]),
        ]);
    }

    @override
    Future<void> post(GeneratedMessage command) => Future.value();

    @override
    Future<E> observeAfterCommand<E extends GeneratedMessage>(GeneratedMessage command) =>
        Future.value();
}
