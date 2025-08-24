package com.xuncorp.spw.workshop.api.config

/**
 * 插件配置管理器接口
 *
 * 允许插件获取和管理多个配置文件。
 * 每个配置文件都由一个专属的 ConfigHelper 实例来表示。
 */
interface ConfigManager {
    /**
     * 获取默认的配置文件帮助程序 (通常对应 config.json)
     *
     * @return 默认的 ConfigHelper 实例
     */
    fun getConfig(): ConfigHelper

    /**
     * 根据文件名获取一个特定的配置文件帮助程序
     *
     * 如果首次请求该文件，管理器会创建一个新的帮助程序实例。
     * 后续对同一文件名的请求将返回缓存的同一实例。
     *
     * @param fileName 配置文件的名称, 例如 "database.json"
     * @return 对应文件的 IConfigHelper 实例
     */
    fun getConfig(fileName: String): ConfigHelper
}