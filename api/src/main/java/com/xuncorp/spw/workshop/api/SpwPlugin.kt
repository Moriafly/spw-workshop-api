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
) : Plugin() {
    /**
     * 插件更新时调用
     *
     * 注意：仅在SPW当中进行更新时调用
     * 如果通过其他方式（如替换文件）更新插件，则不会调用此
     */
    open fun update() {}
}
