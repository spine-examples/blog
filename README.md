# Blog Example

This example provides a blog application implemented with Spine. It allows to create blogs, write blog posts 
and publish them on blog pages so that other people can read them.

## Domain Model  

Blog Bounded Context is composed of two aggregates 
([`BlogAggregate`](./src/main/java/io/spine/examples/blog/BlogAggregate.java) and 
[`BlogPostAggregate`](./src/main/java/io/spine/examples/blog/BlogPostAggregate.java)) and one projection
([`BlogViewProjection`](./src/main/java/io/spine/examples/blog/BlogViewProjection.java)). 

### Blog Aggregate
`BlogAggregate` manages the state of `Blog` model by handling `CreateBlog` command and reacting to `BlogPostCreated` event.

`CreateBlog` command is produced to create a new `Blog` entity. `BlogPostCreated` event is emitted to notify that a new 
`BlogPost` entity was created and it should be assigned to a `Blog`.

### Blog Post Aggregate
`BlogPostAggregate` manages the state of `BlogPost` entity and handles `CreateBlogPost` and `PublishBlogPost` commands.

`CreateBlogPost` command is emitted to create a new `BlogPost` entity with a "draft" state in a `Blog`. 
`PublishBlogPost` command is used to move existing `BlogPost` from "draft" to "published" state and make it visible in `BlogViewProjection`.

### Blog View Projection
`BlogViewProjection` represents the current state of a `Blog` and contains a list of published `BlogPost`s. 
It encapsulates the view that the reader should see when reading the `Blog`.

## Running

A local gRPC server receiving commands and queries for the Blog Bounded Context can be started with: 
```sh
./gradlew :blog:run
```

By default, it listens on the port 50051. To start it on a different port, use:
```sh
/gradlew :blog:run -Dport=PORT_NUMBER
```
