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

import androidx.compose.runtime.Composable
import com.xuncorp.spw.workshop.api.SinceApi
import com.xuncorp.spw.workshop.api.UnstableSpwWorkshopApi

/**
 * 插件直接注册普通方法与 UI Hook 的入口
 *
 * 需要声明并获得 CLASS_TRANSFORM 权限，宿主根据实际调用插件管理归属
 * 可在 start 中同步准备注册，启动成功后安装；启动失败时回滚全部准备的注册
 * 已启动插件也可注册，成功返回表示安装完成，停用、卸载及撤销权限时自动清理
 * 无法识别调用者或权限不足时抛出 PluginPermissionDeniedException
 * 目标缺失、歧义或签名不支持时抛出 IllegalArgumentException，安装失败时抛出 IllegalStateException
 * 首版不支持构造函数、native/abstract、suspend 状态机及已内联调用
 * 宿主内部目标名称随版本与混淆变化，插件应明确支持的宿主版本
 */
@UnstableSpwWorkshopApi
@SinceApi("1.19.0", "0.1.0-dev22")
abstract class HookRegistrar {
    /**
     * 参数类型为 null 时按名称匹配唯一方法，空列表表示无参数
     * 类型使用 JVM 名称，例如 long、java.lang.String，不需要 descriptor
     */
    abstract fun hookMethod(
        className: String,
        methodName: String,
        parameterTypes: List<String>?,
        callback: MethodHook
    ): HookHandle

    fun hookMethod(
        className: String,
        methodName: String,
        callback: MethodHook
    ): HookHandle = hookMethod(className, methodName, null, callback)

    fun hookMethod(
        className: String,
        methodName: String,
        parameterTypes: List<String>? = null,
        priority: Int = 50,
        configure: MethodHookBuilder.() -> Unit
    ): HookHandle = hookMethod(
        className,
        methodName,
        parameterTypes,
        MethodHookBuilder(priority).apply(configure).build()
    )

    /**
     * 替换 inClass 内唯一的 Composable 调用，无有效注册时执行原组件
     * 扫描整个类，包含编译器提取的 lambda，参数类型仅包含业务参数
     * 同位置的替换采用最高优先级注册，同优先级采用最早注册
     */
    abstract fun replaceComposable(
        className: String,
        methodName: String,
        inClass: String,
        parameterTypes: List<String>?,
        priority: Int,
        content: ComposableHook
    ): HookHandle

    fun replaceComposable(
        className: String,
        methodName: String,
        inClass: String,
        content: ComposableHook
    ): HookHandle = replaceComposable(className, methodName, inClass, null, 50, content)

    fun replaceComposable(
        className: String,
        methodName: String,
        inClass: String,
        parameterTypes: List<String>? = null,
        priority: Int = 50,
        content: @Composable (UiHookCall) -> Unit
    ): HookHandle = replaceComposable(
        className,
        methodName,
        inClass,
        parameterTypes,
        priority,
        object : ComposableHook() {
            @Composable
            override fun Content(call: UiHookCall) {
                content(call)
            }
        }
    )

    /**
     * 在 inClass 内唯一的 Composable 调用正常返回后追加组件
     */
    abstract fun afterComposable(
        className: String,
        methodName: String,
        inClass: String,
        parameterTypes: List<String>?,
        priority: Int,
        content: ComposableHook
    ): HookHandle

    fun afterComposable(
        className: String,
        methodName: String,
        inClass: String,
        content: ComposableHook
    ): HookHandle = afterComposable(className, methodName, inClass, null, 50, content)

    fun afterComposable(
        className: String,
        methodName: String,
        inClass: String,
        parameterTypes: List<String>? = null,
        priority: Int = 50,
        content: @Composable (UiHookCall) -> Unit
    ): HookHandle = afterComposable(
        className,
        methodName,
        inClass,
        parameterTypes,
        priority,
        object : ComposableHook() {
            @Composable
            override fun Content(call: UiHookCall) {
                content(call)
            }
        }
    )

    /**
     * 包装具名的普通 Function1 DSL 参数，先执行原构建器，再按优先级追加插件内容
     * 不支持 Composable lambda，作用域类型在执行时核实
     */
    abstract fun <S : Any> appendContent(
        className: String,
        methodName: String,
        inClass: String,
        parameter: String,
        scopeType: Class<S>,
        parameterTypes: List<String>?,
        priority: Int,
        content: ContentHook<S>
    ): HookHandle

    fun <S : Any> appendContent(
        className: String,
        methodName: String,
        inClass: String,
        parameter: String,
        scopeType: Class<S>,
        content: ContentHook<S>
    ): HookHandle = appendContent(
        className,
        methodName,
        inClass,
        parameter,
        scopeType,
        null,
        50,
        content
    )

    inline fun <reified S : Any> appendContent(
        className: String,
        methodName: String,
        inClass: String,
        parameter: String,
        parameterTypes: List<String>? = null,
        priority: Int = 50,
        noinline content: S.(UiHookCall) -> Unit
    ): HookHandle = appendContent(
        className,
        methodName,
        inClass,
        parameter,
        S::class.java,
        parameterTypes,
        priority
    ) { scope, call -> content(scope, call) }
}
