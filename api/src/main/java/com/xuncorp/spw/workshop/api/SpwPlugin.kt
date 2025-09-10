package com.xuncorp.spw.workshop.api

import org.pf4j.Plugin

/**
 * SPW 插件基类
 *
 * 所有 SPW 插件都必须继承自此类
 *
 * @param pluginContext 插件上下文，提供插件运行时的信息
 */
abstract class SpwPlugin(
    val pluginContext: PluginContext
) : Plugin()
