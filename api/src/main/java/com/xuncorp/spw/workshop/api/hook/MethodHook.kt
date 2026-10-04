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

package com.xuncorp.spw.workshop.api.hook

import com.xuncorp.spw.workshop.api.SinceApi
import com.xuncorp.spw.workshop.api.UnstableSpwWorkshopApi

/**
 * 普通 JVM 方法的前置与后置回调
 *
 * before 按优先级从高到低执行，after 对已进入的回调反向执行
 * 同优先级按注册顺序执行，回调在目标方法的调用线程执行
 * before/after 抛出异常时宿主记录错误并恢复该回调之前的参数数组和结果状态
 * 对象内部修改及其他副作用无法撤销
 */
@UnstableSpwWorkshopApi
@SinceApi("1.19.0", "0.1.0-dev22")
abstract class MethodHook
    @JvmOverloads
    constructor(
        val priority: Int = 50
    ) {
        @Throws(Throwable::class)
        open fun before(call: MethodHookParam) {}

        @Throws(Throwable::class)
        open fun after(call: MethodHookParam) {}
    }
