/*
 * SPW Workshop API
 * Copyright (C) 2025 Moriafly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.xuncorp.spw.workshop.api.config

import com.xuncorp.spw.workshop.api.UnstableSpwWorkshopApi

import java.util.function.Consumer

/**
 * 插件配置管理器接口
 *
 * 允许插件获取和管理多个配置文件
 * 每个配置文件都由一个专属的 ConfigHelper 实例来表示
 */
@UnstableSpwWorkshopApi
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
     * 如果首次请求该文件，管理器会创建一个新的帮助程序实例
     * 后续对同一文件名的请求将返回缓存的同一实例
     *
     * @param fileName 配置文件的名称, 例如 "database.json"
     * @return 对应文件的 ConfigHelper 实例
     */
    fun getConfig(fileName: String): ConfigHelper

    /**
     * 添加一个监听器，当默认配置文件 (config.json) 发生更改时调用
     *
     * 监听器会在配置文件被保存后触发，传递被修改的文件名作为参数。
     * 这允许插件对配置更改做出响应，例如重新加载设置或更新状态。
     *
     * @param listener 当指定文件更改时调用，参数为被修改的配置路径和对应的 ConfigHelper 实例
     */
    fun addConfigChangeListener(listener: Consumer<ConfigHelper>)

    /**
     * 添加一个监听器，当指定配置文件发生更改时调用
     *
     * 监听器会在配置文件被保存后触发，传递被修改的文件名作为参数。
     * 这允许插件对配置更改做出响应，例如重新加载设置或更新状态。
     *
     * @param fileName 要监听的配置文件名 默认值为 "config.json"
     * @param listener 当指定文件更改时调用，参数为被修改的配置路径和对应的 ConfigHelper 实例
     */
    fun addConfigChangeListener(fileName: String, listener: Consumer<ConfigHelper>)
}
