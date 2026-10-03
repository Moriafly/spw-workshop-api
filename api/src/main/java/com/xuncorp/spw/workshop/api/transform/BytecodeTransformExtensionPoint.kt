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

package com.xuncorp.spw.workshop.api.transform

import com.xuncorp.spw.workshop.api.PluginPermission
import com.xuncorp.spw.workshop.api.SinceApi
import org.pf4j.ExtensionPoint

/**
 * 字节码转换拓展点
 *
 * 声明后由宿主在插件启动后收集并应用，插件停用或卸载时移除并还原目标类
 *
 * 需要声明并获授 [PluginPermission.CLASS_TRANSFORM] 权限，未授权时转换器不会被应用
 */
@SinceApi("1.19.0", "0.1.0-dev22")
interface BytecodeTransformExtensionPoint : ExtensionPoint {
    /**
     * 返回本插件提供的全部宿主类转换器
     */
    fun getTransformers(): List<HostClassTransformer> = emptyList()
}
