import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

group = "com.gg.example"
version = "1.0.0"

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
    // Kotlin 标准库
    compileOnly(kotlin("stdlib"))

    // SPW Workshop API
    project(":api").let {
        compileOnly(it)
        kapt(it)
    }

//     实际开发中应该使用下面的依赖
//    compileOnly(libs.spw.workshop.api)
//    kapt(libs.spw.workshop.api)
}

// 插件元数据配置
val pluginClass = "com.gg.example.MainPlugin"
val pluginId = "com.gg.example"
val pluginName = "ExamplePlugin"
val pluginDescription = "An example plugin for Salt Player for Windows"
val pluginVersion = "1.0.0"
val pluginProvider = "Zeshi Palace"
val pluginRepository = "https://github.com/Moriafly/spw-workshop-api/tree/main/example"

// 配置主 JAR 任务
tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Plugin-Class" to pluginClass,
            "Plugin-Id" to pluginId,
            "Plugin-Name" to pluginName,
            "Plugin-Description" to pluginDescription,
            "Plugin-Version" to pluginVersion,
            "Plugin-Provider" to pluginProvider,
            "Plugin-Has-Config" to "true",
            "Plugin-Open-Source-Url" to pluginRepository,
        )
    }
}

// 创建插件分发包
tasks.register<Jar>("plugin") {
    destinationDirectory.set(
        file(System.getenv("APPDATA") + "/Salt Player for Windows/workshop/plugins/")
    )
    archiveFileName.set("$pluginName-$pluginVersion.zip")

    into("classes") {
        with(tasks.named<Jar>("jar").get())
    }
    dependsOn(configurations.runtimeClasspath)
    into("lib") {
        from({
            configurations.runtimeClasspath
                .get()
                .filter { it.name.endsWith("jar") }
        })
    }
    archiveExtension.set("zip")
}
