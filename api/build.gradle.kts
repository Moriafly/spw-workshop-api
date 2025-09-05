import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

group = "com.github.Moriafly"
version = "0.1.0-dev10"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21

        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

dependencies {
    api(libs.pf4j)
    api(compose.runtime)
    api(compose.ui)
    api(compose.material)
    api(libs.salt.ui)
    testImplementation(libs.junit)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
