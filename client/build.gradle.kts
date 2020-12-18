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

import com.google.protobuf.gradle.protobuf
import io.spine.dart.gradle.Extension

spine.enableDart()

dependencies {
    protobuf(project(":model"))
}

// Since Proto Dart plugin is not applied in the `plugins` section, Gradle does not generate kotlin DSL for it.
// This will be fixed in future by allowing to configure Dart subprojects via Bootstrap. Meanwhile, we configure
// the `protoDart` extension like this.
extensions.getByType(Extension::class).apply {
    modules["spine_client"] = listOf("spine/*", "google/*")
    mainGeneratedDir.value(project.layout.projectDirectory.dir("lib"))
}

val cleanProto by tasks.registering(Delete::class) {
    delete("$projectDir/proto", "$projectDir/generated")
}

tasks.clean {
    dependsOn(cleanProto)
}
