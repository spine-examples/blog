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

import 'package:flutter/material.dart';

/// Constructs a dialog for creating a new [Blog].
Widget newBlogDialog(BuildContext context, void createBlog(String title)) {
  var titleController = TextEditingController();
  return SimpleDialog(title: Text("Create new blog..."), children: [
    Padding(
      child: TextField(
          decoration: InputDecoration(hintText: "My cool blog"), controller: titleController),
      padding: EdgeInsets.all(8),
    ),
    TextButton(
      child: Text("CREATE"),
      onPressed: () {
        createBlog(titleController.text);
        _closeDialog(context);
      },
    )
  ]);
}

/// Constructs a dialog for creating a new [Post].
Widget newPostDialog(BuildContext context, void createPost(String title, String body)) {
  var titleController = TextEditingController();
  var bodyController = TextEditingController();
  return SimpleDialog(children: [
    TextField(
      decoration: InputDecoration(hintText: "New post"),
      controller: titleController,
    ),
    TextField(
        decoration: InputDecoration(hintText: "Write something"), controller: bodyController),
    Row(children: [
      Expanded(
          child: TextButton(
            child: Text("CANCEL", textAlign: TextAlign.end),
            onPressed: () => _closeDialog(context),
          )),
      TextButton(
        child: Text("POST"),
        onPressed: () {
          createPost(titleController.text, bodyController.text);
          _closeDialog(context);
        },
      )
    ])
  ]);
}

_closeDialog(BuildContext context) => Navigator.of(context, rootNavigator: true).pop();
