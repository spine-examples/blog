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

plugins {
    java

    `idea-sync`
    `static-analysis`
    tests

    id("io.spine.tools.gradle.bootstrap") version("1.8.0")
    id("net.ltgt.errorprone") version("2.0.2")
}

subprojects {
    apply {
        from("$rootDir/version.gradle.kts")
        plugin("java")
        plugin("io.spine.tools.gradle.bootstrap")
        plugin("net.ltgt.errorprone")
        plugin("static-analysis")
        plugin("tests")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    val errorProneVersion: String by extra
    val errorProneJavacVersion: String by extra
    val guavaVersion: String by extra
    val checkerFrameworkVersion: String by extra
    val nettyVersion: String by extra
    val javaxAnnotations: String by extra
    val junitVersion: String by extra

    dependencies {
        errorprone("com.google.errorprone:error_prone_core:${errorProneVersion}")
        errorproneJavac("com.google.errorprone:javac:${errorProneJavacVersion}")
        implementation("javax.annotation:javax.annotation-api:${javaxAnnotations}")
        implementation("com.google.guava:guava:${guavaVersion}")
        implementation("org.checkerframework:checker-qual:${checkerFrameworkVersion}")
        runtimeOnly("io.grpc:grpc-netty:${nettyVersion}")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    }
}
