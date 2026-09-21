package com.xuncorp.spw.workshop.api

/**
 * 插件可申请的宿主权限
 *
 * 在 Gradle 的 spmod.PluginPermissions 中使用打包插件提供的权限枚举声明，由用户在启用插件时授权
 * 声明本身不授予权限，未声明的权限始终未授予
 *
 * @property id Plugin-Permissions 清单属性使用的稳定标识
 */
@SinceApi("1.19.0", "0.1.0-dev21")
enum class PluginPermission(val id: String) {
    /**
     * 注册应用内快捷键，并可通过 hasGlobal 允许用户配置全局快捷键
     */
    KEY_BINDINGS("key-bindings"),

    /**
     * 读取曲库歌曲元数据、文件路径、收藏状态和内嵌封面，包括当前歌曲的元数据查询
     */
    LIBRARY_READ("library-read"),

    /**
     * 写入曲库数据，不包含读取权限
     *
     * 预留给后续曲库写入接口，当前 API 尚未提供写入能力
     */
    LIBRARY_WRITE("library-write")
}
