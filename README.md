# SPW 创意工坊（Mod）API

[![](https://jitpack.io/v/Moriafly/spw-workshop-api.svg)](https://jitpack.io/#Moriafly/spw-workshop-api)

## 介绍

SPW 创意工坊 (Mod) API 是一个为实现 SPW 插件/模块而设计的库，目前尚处于试验阶段。它基于 PF4J (Plugin Framework for Java) 构建，旨在简化 SPW 在 JVM 平台上的插件化开发。

通过结合本库与 Java 反射等高级特性，开发者可以对 SPW 实现更深层次的操作与控制。

该库的核心逻辑代码位于 api 文件夹下的 com.xuncorp.spw.workshop.api 包中。其中，以 ExtensionPoint 结尾的类是插件的拓展点接口，而 WorkshopApi 类则提供了供插件访问的 SPW 公开方法。

## 示例插件

示例插件项目位于 [example](example) 目录下。建议参考该项目以快速上手。

## 使用方法

新建 Kotlin/Java 库项目，在 `gradle/libs.versions.toml` 中添加 API 依赖：

```toml
[versions]
# 0.1.0-dev21 替换为最新的（或需要的）版本
spw-workshop-api = "0.1.0-dev21"

[libraries]
spw-workshop-api = { group = "com.github.Moriafly", name = "spw-workshop-api", version.ref = "spw-workshop-api" }
```

在 `settings.gradle.kts` 中配置 Gradle 插件和库依赖仓库：

```kotlin
pluginManagement {
    repositories {
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.xuncorp.spw.workshop") {
                useModule("com.github.Moriafly:spw-workshop-gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

模块 gradle 类型写法：

对于 Groovy:
```gradle
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '2.3.0'
    id 'org.jetbrains.kotlin.kapt' version '2.3.0'
    id 'com.xuncorp.spw.workshop' version '0.1.0-dev21'
}

dependencies {
    compileOnly 'org.jetbrains.kotlin:kotlin-stdlib'
    compileOnly libs.spw.workshop.api
    kapt libs.spw.workshop.api
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

spmod { config ->
    config.PluginClass = "com.xuncorp.workshop.demo.classical.ClassicalPlugin"
    config.PluginId = "workshop-classical"
    config.PluginVersion = "0.0.9"
    config.PluginProvider = "Xuncorp"
}
```

对于 Kotlin DSL:
```kotlin
plugins {
    id("java-library")
    kotlin("jvm") version "2.3.0"
    kotlin("kapt") version "2.3.0"
    id("com.xuncorp.spw.workshop") version "0.1.0-dev21"
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(libs.spw.workshop.api)
    kapt(libs.spw.workshop.api)
}

spmod {
    PluginClass = "com.xuncorp.workshop.demo.classical.ClassicalPlugin"
    PluginId = "workshop-classical"
    PluginVersion = "0.0.9"
    PluginProvider = "Xuncorp"
}
```

执行 `./gradlew plugin` 后，产物位于该模块的 `build/libs/plugin-<插件 ID>-<插件版本>.spmod`。

插件：

```kotlin
class ClassicalPlugin : SpwPlugin() {
    @Extension
    class PlaybackExtension : PlaybackExtensionPoint {
        // TODO 实现
    }
}
```

## 可用元数据

`spmod` 字段使用大写开头的名称，均可用 `=` 赋值。Groovy 中通过配置对象访问字段，以避免 `PluginId` 与 Gradle 默认导入的同名类型冲突：

| 字段 | Manifest 属性 | 说明 |
| --- | --- | --- |
| `PluginClass` | `Plugin-Class` | 必填，继承自 `SpwPlugin` 的插件主类完整类名 |
| `PluginId` | `Plugin-Id` | 必填，唯一插件 ID，推荐使用 `com.xxx.xxx` 格式 |
| `PluginVersion` | `Plugin-Version` | 必填，插件版本，建议遵循语义化版本规范 |
| `PluginProvider` | `Plugin-Provider` | 可选，插件作者 |
| `PluginName` | `Plugin-Name` | 可选，插件显示名称 |
| `PluginDescription` | `Plugin-Description` | 可选，插件描述 |
| `PluginOpenSourceUrl` | `Plugin-Open-Source-Url` | 可选，插件开源地址 |
| `PluginHasConfig` | `Plugin-Has-Config` | 可选，布尔值，默认 `false`，详见 [配置文件](docs/configs.md) |
| `PluginPermissions` | `Plugin-Permissions` | 可选，权限枚举列表，默认空列表；快捷键需声明 `listOf(PluginPermission.KEY_BINDINGS)`，详见 [插件权限](docs/permissions.md) |

必填字段未设置或为空白时，构建会提示对应的 `spmod` 字段。未设置的可选字符串不会写入 Manifest。

## 混淆配置

SPW 希望 Mod 开源并建议不要混淆其代码。
