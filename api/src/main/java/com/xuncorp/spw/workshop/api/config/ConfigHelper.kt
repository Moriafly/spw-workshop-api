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
import java.nio.file.Path

@UnstableSpwWorkshopApi
interface ConfigHelper {
    /**
     * 获取一个指定类型的配置项 [key] 的值
     *
     * 如果配置文件中不存在该键，或值的类型不匹配，将返回指定的 [defaultValue]
     */
    fun <T> get(
        key: String,
        defaultValue: T
    ): T

    /**
     * 设置一个配置项 [key] 的值 [value]
     *
     * 注意：这个方法仅在内存中更新配置，你需要调用 [save] 方法来将其持久化到磁盘
     *
     * @param key 配置项的键 支持使用点号 (.) 来表示嵌套的 JSON 结构, 例如 "database.host"
     * @param value 要设置的值 (支持 String, Number, Boolean 类型)
     * @throws IllegalArgumentException 如果值的类型不受支持
     */
    fun set(
        key: String,
        value: Any
    )

    /**
     * 保存当前所有配置项到插件的配置文件中
     *
     * @return true 如果保存成功, 否则 false
     */
    fun save(): Boolean

    /**
     * 从磁盘重新加载配置
     *
     * 这会覆盖所有在内存中尚未保存的修改
     *
     * @return true 如果加载成功, 否则 false
     */
    fun reload(): Boolean

    /**
     * 获取当前配置文件的完整路径
     *
     * @return 配置文件的路径
     */
    fun getConfigPath(): Path
}
