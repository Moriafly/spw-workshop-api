// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

tasks.register("publishToMavenLocal") {
    group = "publishing"
    dependsOn(
        ":api:publishToMavenLocal",
        gradle.includedBuild("gradle-plugin").task(":publishToMavenLocal")
    )
}
