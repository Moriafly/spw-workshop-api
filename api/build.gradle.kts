import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    `maven-publish`
}

group = "com.github.Moriafly"
version = "0.1.0-dev21"

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
    api(libs.compose.ui)
    api(libs.compose.foundation)
    api(libs.salt.ui)
    testImplementation(libs.junit)
}

// 旧消费者必须只针对已发布的 dev20 编译，运行时再换成当前 API
val legacyApi by configurations.creating
val legacyFixture = sourceSets.create("legacyFixture")
dependencies {
    legacyApi("com.github.Moriafly:spw-workshop-api:0.1.0-dev20") { isTransitive = false }
    add(legacyFixture.implementationConfigurationName, files(legacyApi))
    add(legacyFixture.implementationConfigurationName, kotlin("stdlib"))
    add(legacyFixture.implementationConfigurationName, libs.pf4j)
    testImplementation(files(legacyFixture.output))
}

tasks.test {
    dependsOn(legacyFixture.classesTaskName)
    doFirst {
        systemProperty("compat.legacyJar", legacyApi.singleFile.absolutePath)
        systemProperty("compat.runtimeClasspath", configurations.testRuntimeClasspath.get().asPath)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "spw-workshop-api"
            from(components["java"])
        }
    }
}
