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

import com.google.protobuf.gradle.*
import io.spine.generate.dart.Extension

buildscript {

    repositories {
        mavenLocal()
        jcenter()
        maven {
            setUrl("https://spine.mycloudrepo.io/public/repositories/releases")
        }
    }

    dependencies {
        classpath("io.spine.tools:spine-proto-dart-plugin:1.6.4")
    }
}

plugins {
    dart
    codegen
}

spine.enableJava()

apply(plugin = "io.spine.tools.proto-dart-plugin")

tasks.assemble {
    dependsOn("generateDart")
}

protobuf {
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("dart")
                remove("spineProtoc")
            }
            task.builtins {
                remove("java")
            }
        }
    }
}

dependencies {
    protobuf(project(":model"))
}

tasks.withType(JavaCompile::class) {
    enabled = false
}

extensions.getByType(Extension::class).apply {
    modules["spine_client"] = listOf("spine/*", "google/*")
    mainGeneratedDir.value(project.layout.projectDirectory.dir("lib"))
}
