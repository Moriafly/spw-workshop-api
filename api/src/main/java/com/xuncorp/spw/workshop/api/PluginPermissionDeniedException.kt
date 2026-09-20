package com.xuncorp.spw.workshop.api

/**
 * 插件调用了未声明或未经用户授权的宿主能力
 *
 * @property pluginId 被拒绝的调用插件 ID
 * @property permission 此次调用需要的权限
 */
@SinceApi("1.19.0", "0.1.0-dev21")
class PluginPermissionDeniedException(
    val pluginId: String,
    val permission: PluginPermission
) : SecurityException("Plugin '$pluginId' has not been granted '${permission.id}'")
