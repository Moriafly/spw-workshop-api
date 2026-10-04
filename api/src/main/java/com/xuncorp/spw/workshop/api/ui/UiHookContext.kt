/*
 * SPW Workshop API
 * Copyright (C) 2026 Moriafly
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

package com.xuncorp.spw.workshop.api.ui

import com.xuncorp.spw.workshop.api.SinceApi
import java.util.Collections

/**
 * Hook 调用位置的参数与内容构建作用域
 *
 * [arguments] 按被调用方法的 JVM 参数顺序排列，仅包含 Composer 之前的业务参数
 * 不包含实例方法接收者、Composer、changed 与 default mask；基本类型自动装箱
 * replaceCall 仅桥接显式参数；appendContent 的其他默认参数尚未由宿主求值，
 * 对应元素可能为 null 或基本类型零值，插件应只读取已确认显式传入的业务参数
 * 列表是不可修改的浅拷贝，其中的对象、回调与作用域仍属于当前宿主调用
 * 不应将上下文保存到组件或内容构建器的生命周期之外
 *
 * @param contentScope appendContent 执行时原内容构建器的接收者，其他 Hook 为 null
 */
@SinceApi("1.19.0", "0.1.0-dev22")
class UiHookContext(
    arguments: Array<out Any?>,
    val contentScope: Any? = null
) {
    val arguments: List<Any?> = Collections.unmodifiableList(arguments.toList())

    companion object {
        @JvmField
        val Empty: UiHookContext = UiHookContext(emptyArray())
    }
}
