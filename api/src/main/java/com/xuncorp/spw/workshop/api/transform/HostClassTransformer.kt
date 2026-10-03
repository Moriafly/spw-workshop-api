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

@file:Suppress("unused")

package com.xuncorp.spw.workshop.api.transform

import com.xuncorp.spw.workshop.api.SinceApi

/**
 * 宿主类字节码转换器
 *
 * 用于插件在运行时修改宿主类的字节码，增强或替换其行为
 *
 * 插件停用或卸载后，目标类将会恢复为原始字节码
 *
 * 限制:
 * - 违反时重转换失败并记录日志，修改不会生效
 * - 注入的字节码只能引用目标类的类加载器（应用类加载器）可见的类，
 *   即 JDK 与宿主自身的类；**不可引用插件自身的类**，否则目标类在运行时会抛出
 *   [NoClassDefFoundError]
 * - suspend 函数编译为状态机，不适合作为转换目标
 *
 * 提示：本契约只传递字节码，插件可自由选用字节码库。逐条写 ASM 指令较繁琐，
 * 推荐用 ByteBuddy `@Advice` 以普通 Java/Kotlin 代码编写增强逻辑（编译期检查），
 * 或用 Javassist 以源码字符串改写；两者打进插件 lib/ 即可，宿主无需感知
 */
@SinceApi("1.19.0", "0.1.0-dev22")
interface HostClassTransformer {
    /**
     * 目标类的二进制名，例如 `com.xuncorp.voxzen.service.PlaybackController`
     */
    val targetClassName: String

    /**
     * 转换目标类的字节码
     *
     * 此函数可能在任意线程上被调用，且同一类可能被多次转换（插件启用、停用、热重载等），
     * 实现应保持幂等，不对同一输入累积修改
     *
     * @param className 目标类的二进制名，与 [targetClassName] 相同
     * @param classfileBuffer 类字节码。多个插件转换同一类时，后注册的插件收到的是前一个插件转换后的结果
     * @return 转换后的类字节码，返回 null 表示不修改
     */
    fun transform(className: String, classfileBuffer: ByteArray): ByteArray?
}
