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
 * 在原普通 DSL 构建器执行后追加内容
 *
 * 与原构建器在相同线程执行，可能随宿主重组重复调用，异常按原构建器异常传播
 * 不能直接调用 Composable，应将组件作为作用域构建方法的 content 参数传入
 */
@SinceApi("1.19.0", "0.1.0-dev22")
fun interface ContentHook<S : Any> {
    @Throws(Throwable::class)
    fun buildContent(scope: S, call: UiHookCall): Unit
}
