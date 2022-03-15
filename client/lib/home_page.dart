/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import 'package:blog_client/blog/commands.pb.dart';
import 'package:blog_client/blog/events.pb.dart';
import 'package:blog_client/client.dart';
import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';

import 'blog/blog.pb.dart';
import 'blog/identifiers.pb.dart';
import 'dialogs.dart';

/// The Blog app home page.
///
/// Displays all the blogs and allows to view all of their posts.
///
class BlogHomePage extends StatefulWidget {

    BlogHomePage(this.title, this._client);

    final String title;
    final Client _client;

    @override
    _BlogPageState createState() => _BlogPageState(_client);
}

class _BlogPageState extends State<BlogHomePage> {

    final Client _client;

    Map<BlogId, Blog> blogs = {};
    BlogView? displayedBlog;

    _BlogPageState(this._client);

    /// Adds or updates the given [Blog] to the list and triggers the UI update.
    void addBlog(Blog blog) {
        setState(() {
            blogs[blog.id] = blog;
        });
    }

    /// Displays the posts of the given Blog and triggers the UI update.
    void display(BlogView blog) {
        setState(() {
            displayedBlog = blog;
        });
    }

    @override
    void initState() {
      super.initState();
      _fetchBlogs();
    }

    /// Fetches all the [Blog]s from the server.
    void _fetchBlogs() {
        _client.fetchBlogs().forEach(addBlog);
    }

    /// Fetches the posts of the blog with the given ID.
    ///
    /// The fetched blog is then displayed on the screen.
    ///
    void _fetchBlogWithPosts(BlogId id) {
        _client.fetchBlogWithPosts(id).then(display);
    }

    /// Posts a command to create a new [Blog] with the given title.
    void _newBlog(String title) {
        var id = BlogId()..uuid = Uuid().v4();
        var command = CreateBlog()
            ..id = id
            ..title = title;
        _client.observeAfterCommand<BlogCreated>(command)
               .then((_) => _fetchBlogs());
    }

    /// Posts a command to create a new [Post] with the given title and body.
    ///
    /// The post is created into the current [displayedBlog].
    ///
    void _newPost(String title, String body) {
        var id = PostId()..uuid = Uuid().v4();
        var blogId = displayedBlog!.id;
        var command = CreatePost()
            ..id = id
            ..blog = blogId
            ..title = title
            ..body = body;
        _client.observeAfterCommand<PostCreated>(command)
               .then((_) => _publishPost(id, blogId));
    }

    void _publishPost(PostId post, BlogId blog) {
        var command = PublishPost()
            ..post = post
            ..blog = blog;
        _client.observeAfterCommand<PostPublished>(command)
               .then((_) => _fetchBlogWithPosts(blog));
    }

    @override
    Widget build(BuildContext context) {
      return Scaffold(
          appBar: AppBar(
              title: Text(widget.title),
          ),
          floatingActionButton: Visibility(
              child: FloatingActionButton(
                  child: Icon(Icons.add),
                  onPressed: () => showDialog(
                      context: context,
                      builder: (context) => newPostDialog(context, _newPost)
                  ),
              ),
              visible: displayedBlog != null,
          ),
          body: Row(
              children: [
                  Expanded(child: _blogList(context), flex: 1),
                  Expanded(child: Column(key: _blogKey(), children: _postList()), flex: 3)
              ],
          ));
    }

    Key _blogKey() {
        return displayedBlog != null ? _BlogKey(displayedBlog!.id) : Key('No data');
    }

    List<Widget> _postList() {
        return displayedBlog != null && displayedBlog!.post.isNotEmpty
            ? _bakePosts()
            : [_empty('Nothing to see here')];
    }

    Widget _blogList(BuildContext context) {
        var newBlogButton = Container(
            color: Colors.redAccent,
            child: TextButton(
              child: Text("NEW BLOG", style: TextStyle(color: Colors.white)),
              onPressed: () {
                  showDialog(context: context,
                             builder: (context) => newBlogDialog(context, _newBlog));
              },
            ));
        List<Widget> items = [];
        items.add(newBlogButton);
        if (blogs.isNotEmpty) {
            var blogs = _bakeBlogs();
            items.addAll(blogs);
        }
        Widget content = ListView(children: items);
        var container = Container(child: content, color: Colors.white);
        return container;
    }

    List<Widget> _bakeBlogs() => blogs.entries
        .map((blog) => GestureDetector(
            onTap: () => _fetchBlogWithPosts(blog.key),
            child: Padding(
                child: Row(key: _BlogKey(blog.key), children: _displayBlog(blog.value)),
                padding: EdgeInsets.only(bottom: 8),
            )))
        .toList(growable: true);

    List<Widget> _displayBlog(Blog blog) => [
          Text(blog.title, textScaleFactor: 1.5),
          Text('${blog.post.length} posts', textScaleFactor: 0.75)
    ];

    List<Widget> _bakePosts() => displayedBlog!.post.expand(_displayPost).toList(growable: false);

    List<Widget> _displayPost(PostItem post) =>
        [Text(post.title, textScaleFactor: 1.5, softWrap: true), Text(post.body, softWrap: true)];

    Widget _empty(String text) =>
        Center(child: Text(text, style: TextStyle(color: Colors.blueGrey, fontSize: 30)));
}

/// A widget `Key` which identifies a widget by a `BlogId`.
///
/// Flutter uses `Key`s to identify whether or not to rebuild a section of the widget tree.
/// See [Key] for more info.
///
class _BlogKey extends Key {
    final BlogId id;

    _BlogKey(this.id) : super.empty();

    @override
    bool operator ==(Object other) =>
        identical(this, other) ||
        other is _BlogKey && runtimeType == other.runtimeType && id == other.id;

    @override
    int get hashCode => id.hashCode;
}
