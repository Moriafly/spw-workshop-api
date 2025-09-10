package com.xuncorp.spw.workshop.api

/**
 * 插件上下文信息
 *
 * 提供插件所需的基本信息，如 ID、版本和路径
 *
 * @param pluginId 插件 ID
 * @param pluginVersion 插件版本
 * @param pluginPath 插件安装路径
 * @param spwVersion SPW 版本
 * @param spwChannel SPW 渠道
 */
data class PluginContext(
    val pluginId: String,
    val pluginVersion: String,
    val pluginPath: String,
    val spwVersion: String,
    val spwChannel: Channel
)

enum class Channel {
    Steam,
    MS
}
