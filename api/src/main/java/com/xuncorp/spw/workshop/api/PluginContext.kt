package com.xuncorp.spw.workshop.api

/**
 * 插件上下文信息
 *
 * 提供插件所需的基本信息，如 ID、版本、路径和更新 URL
 *
 * @property pluginId 插件 ID
 * @property pluginVersion 插件版本
 * @property pluginPath 插件安装路径
 * @property spwVersion SPW 版本
 * @property spwChannel SPW 渠道
 * @property pluginUpdateUrl 插件更新URL
 */
data class PluginContext(
    val pluginId: String,
    val pluginVersion: String,
    val pluginPath: String,
    val spwVersion: String,
    val spwChannel: Channel,
    val pluginUpdateUrl: String? = null
)

enum class Channel {
    Steam,
    MS
}