plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.github.Moriafly.spw-workshop-api"
version = "0.1.0-dev22"

repositories {
    mavenCentral()
}

dependencies {
    // 与宿主 PF4J 使用相同的表达式解析器
    implementation("com.github.zafarkhaja:java-semver:0.10.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.encoding = "UTF-8"
}

gradlePlugin {
    plugins {
        create("workshop") {
            id = "com.xuncorp.spw.workshop"
            implementationClass = "com.xuncorp.spw.workshop.gradle.WorkshopGradlePlugin"
            displayName = "Salt Player Plugin Packaging"
            description = "用于打包为 Salt Player 使用的 .spmod 分发包"
        }
    }
}
