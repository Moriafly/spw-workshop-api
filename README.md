# SPW 创意工坊（Mod）API

[![](https://jitpack.io/v/Moriafly/spw-workshop-api.svg)](https://jitpack.io/#Moriafly/spw-workshop-api)

## 介绍

SPW 创意工坊（Mod）API 是用于实现 SPW 插件/模块的库。基于 PF4J 实现，目前还在试验阶段。

SPW 基于 JVM 平台，插件化比较简单，本库配合反射等操作可以对 SPW 进行更高级的操作控制。

本库的 api 文件夹下是主要逻辑支持代码，在包 com.xuncorp.spw.workshop.api 下。

其中类名称后边为 ExtensionPoint 的类表示插件拓展点，WorkshopApi 是供插件访问 SPW 公开的方法。

## 使用方法

新建 Kotlin/Java 库项目，添加依赖（libs.version.toml）：

```toml
[versions]
# 0.1.0-dev05 替换为最新的（或需要的）版本
spw-workshop-api = "0.1.0-dev05"

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

## 混淆配置

```
-keep class com.xuncorp.spw.workshop.api.** { *; }
```
