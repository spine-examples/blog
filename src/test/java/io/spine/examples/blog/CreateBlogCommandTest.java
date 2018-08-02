package io.spine.examples.blog;

import io.spine.examples.blog.commands.CreateBlog;
import io.spine.examples.blog.events.BlogCreated;
import io.spine.server.entity.Repository;
import io.spine.testing.server.aggregate.AggregateCommandTest;
import org.junit.jupiter.api.Test;

import static io.spine.base.Identifier.newUuid;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateBlogCommandTest extends AggregateCommandTest<BlogId, CreateBlog, Blog, BlogAggregate> {

    @Override
    protected BlogId newId() {
        return BlogId.newBuilder().setValue(newUuid()).build();
    }

    @Override
    protected CreateBlog createMessage() {
        return CreateBlog.newBuilder()
                .setBlogId(id())
                .setName("TestBlog")
                .build();
    }

    @Override
    protected Repository<BlogId, BlogAggregate> createEntityRepository() {
        return new BlogAggregateRepository();
    }

    @Test
    void test() {
        this.expectThat(new BlogAggregate(id()))
                .producesEvent(BlogCreated.class, created -> {
                    assertEquals(id(), created.getBlogId());
                    assertEquals("TestBlog", created.getName());
                });
    }
}
