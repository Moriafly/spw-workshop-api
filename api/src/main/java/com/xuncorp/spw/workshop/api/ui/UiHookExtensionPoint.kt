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

import androidx.compose.runtime.Composable
import com.xuncorp.spw.workshop.api.PluginPermission
import com.xuncorp.spw.workshop.api.SinceApi
import com.xuncorp.spw.workshop.api.WorkshopApi
import org.pf4j.ExtensionPoint

/**
 * 插件自行通过字节码 Hook 注入的 Compose 内容
 *
 * 宿主收集已启动插件的拓展并管理其归属，需声明且获授 [PluginPermission.CLASS_TRANSFORM]
 * 插件用字节码转换器在有效组合位置调用 [WorkshopApi.Ui.renderHook]
 * 本接口不预设插入位置，也不修改宿主已有 UI；相应字节码转换仍由插件提供
 *
 * 停用或卸载后宿主移除内容；重新启用、重新加载后的组合使用新的生命周期
 * 插件应通过 remember、DisposableEffect、LaunchedEffect 等管理组件状态与资源
 * 内容异常按普通 Composable 异常传播
 *
 * 插件必须启用 Compose compiler，并与宿主共享兼容的 Compose、Kotlin 与 UI 组件依赖
 * 不应将这些宿主依赖打入插件包形成第二套运行时
 */
@SinceApi("1.19.0", "0.1.0-dev22")
interface UiHookExtensionPoint : ExtensionPoint {
    /**
     * 插件内唯一且非空白的 Hook ID，同一拓展实例的值保持不变
     *
     * 同一插件的重复 ID 与空白 ID 均不注册，宿主记录错误
     */
    val hookId: String

    /**
     * 在调用 Hook 的组合位置绘制内容，布局作用域与 CompositionLocal 由该位置决定
     *
     * 同一 Hook 可在多个位置调用，每个位置拥有独立组合状态
     */
    @Composable
    fun Content(context: UiHookContext): Unit {}

    /**
     * 向原内容构建器追加内容，例如向菜单作用域添加 item
     *
     * 此方法是普通回调，不能直接调用 Composable；应将组件作为构建器的 content 参数传入
     * 与原构建器在同一线程执行，可随宿主重组多次调用，不在此创建长期资源
     * 插件应按所支持的宿主版本核实 [UiHookContext.contentScope] 及业务参数类型
     * 停用或卸载后不再追加内容，已有界面在后续组合中更新
     */
    fun buildContent(context: UiHookContext): Unit {}
}
