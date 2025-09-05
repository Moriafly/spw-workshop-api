# SPW 创意工坊（Mod）API

[![](https://jitpack.io/v/Moriafly/spw-workshop-api.svg)](https://jitpack.io/#Moriafly/spw-workshop-api)

## 介绍

SPW 创意工坊 (Mod) API 是一个为实现 SPW 插件/模块而设计的库，目前尚处于试验阶段。它基于 PF4J (Plugin Framework for Java) 构建，旨在简化 SPW 在 JVM 平台上的插件化开发。

通过结合本库与 Java 反射等高级特性，开发者可以对 SPW 实现更深层次的操作与控制。

该库的核心逻辑代码位于 api 文件夹下的 com.xuncorp.spw.workshop.api 包中。其中，以 ExtensionPoint 结尾的类是插件的拓展点接口，而 WorkshopApi 类则提供了供插件访问的 SPW 公开方法。

## 示例插件

示例插件项目位于 [example](example) 目录下。建议参考该项目以快速上手。

## 使用方法

新建 Kotlin/Java 库项目，添加依赖（libs.version.toml）：

```toml
[versions]
# 0.1.0-dev10 替换为最新的（或需要的）版本
spw-workshop-api = "0.1.0-dev10"

[libraries]
spw-workshop-api = { group = "com.github.Moriafly", name = "spw-workshop-api", version.ref = "spw-workshop-api" }
```

模块 gradle 类型写法：

对于 Groovy:
```gradle
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '2.0.21'
    id 'org.jetbrains.kotlin.kapt' version '2.0.21'
}

dependencies {
    compileOnly 'org.jetbrains.kotlin:kotlin-stdlib'
    compileOnly libs.spw.workshop.api
    kapt libs.spw.workshop.api
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

def pluginClass = "com.xuncorp.workshop.demo.classical.ClassicalPlugin"
def pluginId = "workshop-classical"
def pluginVersion = "0.0.9"
def pluginProvider = "Xuncorp"

tasks.named("jar") {
    manifest {
        attributes["Plugin-Class"] = pluginClass
        attributes["Plugin-Id"] = pluginId
        attributes["Plugin-Version"] = pluginVersion
        attributes["Plugin-Provider"] = pluginProvider
    }
}

tasks.register("plugin", Jar) {
    archiveBaseName.set("plugin-" + pluginId + "-" + pluginVersion)

    into("classes") {
        with(tasks.named("jar").get())
    }
    dependsOn(configurations.runtimeClasspath)
    into("lib") {
        from({
            configurations.runtimeClasspath
                    .filter { it.name.endsWith("jar") }
        })
    }
    archiveExtension = 'zip'
}
```

对于 Kotlin DSL:
```kotlin
plugins {
    id("java-library")
    alias(libs.plugins.jetbrainsKotlinJvm)
    kotlin("kapt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(libs.spw.workshop.api)
    kapt(libs.spw.workshop.api)
}

val pluginClass = "com.xuncorp.workshop.demo.classical.ClassicalPlugin"
val pluginId = "workshop-classical"
val pluginVersion = "0.0.9"
val pluginProvider = "Xuncorp"

tasks.named<Jar>("jar") {
    manifest {
        attributes["Plugin-Class"] = pluginClass
        attributes["Plugin-Id"] = pluginId
        attributes["Plugin-Version"] = pluginVersion
        attributes["Plugin-Provider"] = pluginProvider
    }
}

tasks.register<Jar>("plugin") {
    archiveBaseName.set("plugin-$pluginId-$pluginVersion")

    into("classes") {
        with(tasks.named<Jar>("jar").get())
    }
    dependsOn(configurations.runtimeClasspath)
    into("lib") {
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
        })
    }
    archiveExtension.set("zip")
}
```

插件：

```kotlin
class ClassicalPlugin : Plugin() {
    @Extension
    class PlaybackExtension : PlaybackExtensionPoint {
        // TODO 实现
    }
}
```

## 可用元数据
插件的可用元数据如下：
- Plugin-Class: 插件主类，必须继承自 org.pf4j.Plugin
- Plugin-Id: 插件 ID，必须唯一
- Plugin-Version: 插件版本，建议遵循语义化版本规范
- Plugin-Provider: 插件作者
- Plugin-Open-Source-Url: 插件开源地址（可选）
- Plugin-Has-Config: 插件是否有配置文件（可选），值为 "true" 或 "false" 详细见 [配置文件](docs/configs.md)

## 混淆配置

SPW 希望 Mod 开源并建议不要混淆其代码。
