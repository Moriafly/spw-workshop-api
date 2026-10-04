/*
 * SPW Workshop API
 * Copyright (C) 2026 Zeshi Palace
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

package com.xuncorp.spw.workshop.api.hook

import com.xuncorp.spw.workshop.api.SinceApi
import com.xuncorp.spw.workshop.api.UnstableSpwWorkshopApi

/**
 * UI 调用位置的业务参数，仅在当前组件或构建器生命周期内有效
 *
 * arguments 是不可修改的浅拷贝，不包含实例接收者、Composer、changed 和 default mask
 * 基本类型自动装箱，具名访问依赖目标方法的源码参数信息
 */
@UnstableSpwWorkshopApi
@SinceApi("1.19.0", "0.1.0-dev22")
abstract class UiHookCall {
    abstract val thisObject: Any?
    abstract val arguments: List<Any?>

    fun getArgument(index: Int): Any? = arguments[index]

    abstract fun getArgument(name: String): Any?

    @Suppress("RemoveRedundantQualifierName")
    fun <T> getArgument(name: String, type: Class<T>): T? {
        val value = getArgument(name) ?: return null
        val boxed = when (type) {
            java.lang.Boolean.TYPE -> Boolean::class.javaObjectType
            java.lang.Byte.TYPE -> Byte::class.javaObjectType
            java.lang.Character.TYPE -> Char::class.javaObjectType
            java.lang.Short.TYPE -> Short::class.javaObjectType
            java.lang.Integer.TYPE -> Int::class.javaObjectType
            java.lang.Long.TYPE -> Long::class.javaObjectType
            java.lang.Float.TYPE -> Float::class.javaObjectType
            java.lang.Double.TYPE -> Double::class.javaObjectType
            else -> type
        }
        require(boxed.isInstance(value)) { "UI argument '$name' is not ${type.name}" }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    inline fun <reified T> argument(name: String): T = getArgument(name) as T

    inline fun <reified T> argument(index: Int): T = getArgument(index) as T
}
