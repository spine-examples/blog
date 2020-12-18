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

import 'dart:math';

import 'package:firebase/firebase_io.dart' as fb;
import 'package:protobuf/protobuf.dart';
import 'package:spine_client/google/protobuf/empty.pb.dart';
import 'package:spine_client/rest_firebase_client.dart';
import 'package:spine_client/spine/core/ack.pb.dart';
import 'package:spine_client/spine/core/response.pb.dart';
import 'package:spine_client/spine_client.dart';

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
    Future<Ack> post(GeneratedMessage command);
}

/// A client which sends queries and commands over the network to a real server.
class NetworkClient extends Client {

    final BackendClient _backend;
    final ActorRequestFactory _factory;

    /// Creates a new `NetworkClient` with the given server and Firebase.
    ///
    /// Note that `NetworkClient` uses Spine's Firebase `RestClient` which works on all platforms
    /// but does not support subscriptions. Change it to `WebClient` to support subscriptions (only
    /// in browser).
    ///
    NetworkClient(String serverUrl, String firebaseUrl)
        : _backend = BackendClient(serverUrl,
                                   firebase: RestClient(fb.FirebaseClient.anonymous(), firebaseUrl),
                                   typeRegistries: [blogTypes.types()]),
          _factory = ActorRequestFactory(UserId()..value = 'Example Dart client');

    @override
    Stream<Blog> fetchBlogs() {
        var query = _factory.query().all(Blog.getDefault());
        return _backend.fetch<Blog>(query);
    }

    @override
    Future<BlogView> fetchBlogWithPosts(BlogId id) {
        var query = _factory.query().byIds(BlogView.getDefault(), [id]);
        return _backend.fetch<BlogView>(query).first;
    }

    @override
    Future<Ack> post(GeneratedMessage command) {
        var cmd = _factory.command().create(command);
        return _backend.post(cmd);
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
    Future<Ack> post(GeneratedMessage command) async {
        var ok = Status()..ok = Empty.getDefault();
        return Ack()..status = ok;
    }
}
