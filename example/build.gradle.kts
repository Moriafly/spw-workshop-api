import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    id("com.xuncorp.spw.workshop")
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
spmod {
    PluginClass = "com.gg.example.MainPlugin"
    PluginId = "com.gg.example"
    PluginName = "ExamplePlugin"
    PluginDescription = "An example plugin for Salt Player for Windows"
    PluginVersion = "1.0.0"
    PluginProvider = "Zeshi Palace"
    PluginHasConfig = true
    PluginOpenSourceUrl = "https://github.com/Moriafly/spw-workshop-api/tree/main/example"
}
