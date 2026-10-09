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
 * Kotlin 方法 Hook DSL，三个回调块各声明一次
 */
@UnstableSpwWorkshopApi
@SinceApi("1.19.0", "0.1.0-dev22")
class MethodHookBuilder internal constructor(
    private val priority: Int
) {
    private var before: ((MethodHookParam) -> Unit)? = null
    private var replacement: ((MethodHookParam) -> Any?)? = null
    private var after: ((MethodHookParam) -> Unit)? = null

    fun before(callback: (MethodHookParam) -> Unit) {
        check(before == null) { "before is already configured" }
        before = callback
    }

    /**
     * 以回调结果替换原方法，异常作为此次目标方法调用的异常交给 after
     *
     * before 已设置结果或异常时不执行此块
     */
    fun replace(callback: (MethodHookParam) -> Any?) {
        check(replacement == null) { "replace is already configured" }
        replacement = callback
    }

    fun after(callback: (MethodHookParam) -> Unit) {
        check(after == null) { "after is already configured" }
        after = callback
    }

    internal fun build(): MethodHook {
        val beforeCallback = before
        val replacementCallback = replacement
        val afterCallback = after
        return object : MethodHook(priority) {
            override fun before(call: MethodHookParam) {
                beforeCallback?.invoke(call)
                if (!call.isOriginalSkipped && replacementCallback != null) {
                    val result = try {
                        replacementCallback(call)
                    } catch (failure: Throwable) {
                        call.setThrowable(failure)
                        return
                    }
                    call.setResult(result)
                }
            }

            override fun after(call: MethodHookParam) {
                afterCallback?.invoke(call)
            }
        }
    }
}
