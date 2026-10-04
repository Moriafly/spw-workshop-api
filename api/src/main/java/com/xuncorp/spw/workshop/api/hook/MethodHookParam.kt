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
import java.lang.reflect.Method

/**
 * 宿主创建的单次方法调用上下文，仅在此次同步调用期间有效
 *
 * args 包含装箱后的实际参数，before 的修改会传入原方法
 * 静态方法的 thisObject 为 null
 * before 中设置结果或异常会跳过原方法及后续 before，after 中设置则修改最终结果
 * setResult(null) 也表示明确返回，void 方法接受 null 或 Kotlin Unit
 * 不能将上下文保存或交给其他线程，结果和参数必须符合目标方法的 JVM 类型
 */
@UnstableSpwWorkshopApi
@SinceApi("1.19.0", "0.1.0-dev22")
abstract class MethodHookParam {
    abstract val method: Method
    abstract val thisObject: Any?
    abstract val args: Array<Any?>
    abstract val result: Any?
    abstract val throwable: Throwable?
    abstract val isOriginalSkipped: Boolean

    abstract fun setResult(result: Any?)

    abstract fun setThrowable(throwable: Throwable)

    /**
     * 使用当前参数调用原方法，绕过当前方法此次入口的全部 Hook
     *
     * 原方法内部的正常递归调用仍触发 Hook，异常保持原类型
     * 本方法不自动设置结果，需要调用者返回该值或调用 setResult
     */
    @Throws(Throwable::class)
    abstract fun invokeOriginal(): Any?
}
