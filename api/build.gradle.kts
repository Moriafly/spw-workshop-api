import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

group = "com.github.Moriafly"
version = "0.1.0-dev22"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

dependencies {
    api(libs.pf4j)
    api(libs.compose.ui)
    api(libs.compose.foundation)
    api(libs.salt.ui)
    testImplementation(libs.junit)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "spw-workshop-api"
            from(components["java"])
        }
    }
}
