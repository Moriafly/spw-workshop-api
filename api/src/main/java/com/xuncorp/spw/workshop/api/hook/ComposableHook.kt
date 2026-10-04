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

import androidx.compose.runtime.Composable
import com.xuncorp.spw.workshop.api.SinceApi

/**
 * 在宿主有效组合位置绘制的插件内容
 *
 * 继承该位置的 CompositionLocal 与布局环境，状态和 Effect 由组合生命周期管理
 * 内容异常按普通 Composable 异常传播
 * Java 可注册由 Kotlin Compose compiler 编译的实现，不需要手动传递 Composer
 */
@SinceApi("1.19.0", "0.1.0-dev22")
abstract class ComposableHook {
    @Composable
    abstract fun Content(call: UiHookCall): Unit
}
