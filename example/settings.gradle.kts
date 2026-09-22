pluginManagement {
    repositories {
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
    plugins {
        id("org.jetbrains.kotlin.kapt") version "2.3.0"
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.xuncorp.spw.workshop") {
                useModule(
                    "com.github.Moriafly.spw-workshop-api:spw-workshop-gradle-plugin:${requested.version}"
                )
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "example-plugin"
