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

import 'package:blog_client/blog/commands.pb.dart';
import 'package:blog_client/client.dart';
import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';

import 'blog/blog.pb.dart';
import 'blog/identifiers.pb.dart';

void main() {
    var client = NetworkClient('localhost:4242', 'localhost:4242');
    runApp(BlogApp(client));
}

class BlogApp extends StatelessWidget {

    final Client _client;

    BlogApp(this._client);

    @override
    Widget build(BuildContext context) {
        return MaterialApp(
            title: 'Blog',
            theme: ThemeData(
                primarySwatch: Colors.red,
                visualDensity: VisualDensity.adaptivePlatformDensity,
            ),
            home: BlogHomePage('Spine example: Blog', _client),
        );
    }
}

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
    BlogView displayedBlog;

    _BlogPageState(this._client);

    void addBlogs(List<Blog> moreBlogs) {
        Map<BlogId, Blog> newBlogs = Map.fromIterable(moreBlogs,
                                                      key: (blog) => blog.id,
                                                      value: (blog) => blog);
        setState(() {
            blogs.addAll(newBlogs);
        });
    }

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

    void _fetchBlogs() {
        _client.fetchBlogs().toList().then(addBlogs, onError: () => _fetchBlogs());
    }

    void _fetchBlogWithPosts(BlogId id) {
        _client.fetchBlogWithPosts(id).then(display);
    }

    void _newBlog(String title) {
        var id = BlogId()
            ..uuid = Uuid().v4();
        var command = CreateBlog()
            ..id = id
            ..title = title;
        _client.post(command).then((value) => _fetchBlogs());
    }

    void _newPost(String title, String content) {
        var id = PostId()
            ..uuid = Uuid().v4();
        var command = CreatePost()
            ..id = id
            ..blog = displayedBlog.id
            ..title = title
            ..body = content;
        _client.post(command).then((value) => _fetchBlogWithPosts(displayedBlog.id));
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
                        builder: _newPostDialog
                    ),
                ),
                visible: displayedBlog != null,
            ),
            body: Row(
                children: [
                    Expanded(
                        child: _blogList(context),
                        flex: 1
                    ),
                    Expanded(
                        child: Column(key: _blogKey(), children: _postList()),
                        flex: 3
                    )
                ],
            )
        );
    }

    Key _blogKey() {
        return displayedBlog != null ? _BlogKey(displayedBlog.id) : Key('No data');
    }

    List<Widget> _postList() {
        return displayedBlog != null ? _bakePosts() : [_empty('Nothing to see here')];
    }

    Widget _blogList(BuildContext context) {
        var newBlogButton = Container(
            color: Colors.redAccent,
            child: TextButton(
                child: Text(
                    "NEW BLOG",
                    style: TextStyle(color: Colors.white)
                ),
                onPressed: () {
                    showDialog(
                        context: context,
                        builder: _newBlogDialog
                    );
                },
        ));
        Widget content;
        if (blogs.isNotEmpty) {
            var blogs = _bakeBlogs();
            List<Widget> items = [];
            items.add(newBlogButton);
            items.addAll(blogs);
            content = ListView(children: items);
        } else {
            content = newBlogButton;
        }
        var container = Container(
            child: content,
            color: Colors.white
        );
        return container;
    }

    Widget _newBlogDialog(BuildContext context) {
        var titleController = TextEditingController();
        return SimpleDialog(
            title: Text("Create new blog..."),
            children: [
                Padding(
                    child: TextField(
                        decoration: InputDecoration(hintText: "My cool blog"),
                        controller: titleController
                    ),
                    padding: EdgeInsets.all(8),
                ),
                TextButton(
                    child: Text("CREATE"),
                    onPressed: () {
                        _newBlog(titleController.text);
                        _closeDialog(context);
                    },
                )
            ]
        );
    }

    Widget _newPostDialog(BuildContext context) {
        var titleController = TextEditingController();
        var bodyController = TextEditingController();
        return SimpleDialog(
            children: [
                TextField(
                    decoration: InputDecoration(hintText: "New post"),
                    controller: titleController,
                ),
                TextField(
                    decoration: InputDecoration(hintText: "Write something"),
                    controller: bodyController
                ),
                Row(children: [
                    Expanded(child: TextButton(
                        child: Text("CANCEL", textAlign: TextAlign.end),
                        onPressed: () => _closeDialog(context),
                    )),
                    TextButton(
                        child: Text("POST"),
                        onPressed: () {
                            _newPost(titleController.text, bodyController.text);
                            _closeDialog(context);
                        },
                    )
                ])
            ]
        );
    }

    _closeDialog(BuildContext context) =>
        Navigator.of(context, rootNavigator: true).pop();

    List<Widget> _bakeBlogs() => blogs.entries.map((blog) => GestureDetector(
        onTap: () => _fetchBlogWithPosts(blog.key),
        child: Padding(
            child: Row(
                key: _BlogKey(blog.key),
                children: _displayBlog(blog.value)
            ),
            padding: EdgeInsets.only(bottom: 8),
    ))).toList(growable: true);

    List<Widget> _displayBlog(Blog blog) => [
        Text(blog.title, textScaleFactor: 1.5),
        Text('${blog.post.length} posts', textScaleFactor: 0.75)
    ];

    List<Widget> _bakePosts() => displayedBlog
        .post
        .expand(_displayPost)
        .toList(growable: false);

    List<Widget> _displayPost(PostItem post) => [
        Text(post.title, textScaleFactor: 1.5, softWrap: true),
        Text(post.body, softWrap: true)
    ];

    Widget _empty(String text) => Center(
        child: Text(text, style: TextStyle(
            color: Colors.blueGrey,
            fontSize: 30
        ))
    );
}

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
