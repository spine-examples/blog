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

plugins {
    pmd
    java
}

apply(from = "$rootDir/version.gradle.kts")

/*
 * Configure Gradle PMD plugin.
 *
 * Currently a warning on "use incremental analysis" is always emitted. 
 * But there is no way to enable it due to a Gradle issue.
 *
 * See https://github.com/gradle/gradle/issues/8277.
 */

pmd {
    toolVersion = project.extra["pmdVersion"] as String
    isConsoleOutput = true

    // The build is going to fail in case of violations.
    isIgnoreFailures = false

    // Disable the default rule set to use the custom rules (see below).
    ruleSets = listOf()

    // A set of custom rules.
    ruleSetFiles = files("$rootDir/gradle/pmd.xml")

    reportsDir = file("$projectDir/build/reports/pmd")

    // Just analyze the main sources; do not analyze tests.
    sourceSets = listOf(project.sourceSets.main.get())
}

afterEvaluate {
    tasks.withType(JavaCompile::class) {

        // Configure Error Prone:
        //
        // For more config details see:
        //    https://github.com/tbroyer/gradle-errorprone-plugin/tree/master#usage
        //
        (options as ExtensionAware).extensions["errorprone"].withGroovyBuilder {
            val args = getProperty("errorproneArgs") as ListProperty<String>
            args.addAll(listOf(
                    // 1. Exclude generated sources from being analyzed by Error Prone.
                    "-XepExcludedPaths:.*/generated/.*",

                    // 2. Turn the check off until Error Prone can handle `@Nested` JUnit classes.
                    //    See issue: https://github.com/google/error-prone/issues/956
                    "-Xep:ClassCanBeStatic:OFF",

                    // 3. Turn off checks which report unused methods and unused method parameters.
                    //    See issue: https://github.com/SpineEventEngine/config/issues/61
                    "-Xep:UnusedMethod:OFF",
                    "-Xep:UnusedVariable:OFF",
                    "-Xep:CheckReturnValue:OFF"
            ))
        }
    }
}


