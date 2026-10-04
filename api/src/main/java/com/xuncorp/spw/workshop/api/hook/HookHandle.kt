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

package com.xuncorp.spw.workshop.api.hook

import com.xuncorp.spw.workshop.api.SinceApi

/**
 * 一次 Hook 注册的清理句柄
 *
 * unhook 与 close 可重复调用，插件停用或卸载时宿主自动清理注册
 */
@SinceApi("1.19.0", "0.1.0-dev22")
interface HookHandle : AutoCloseable {
    fun unhook(): Unit

    override fun close(): Unit = unhook()
}
