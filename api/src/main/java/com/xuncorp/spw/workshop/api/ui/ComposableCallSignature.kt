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

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * 普通 Compose JVM 方法调用的参数布局：业务参数、Composer、changed masks、default masks。
 *
 * 默认参数解析以 Kotlin / Compose compiler 2.3.0、Compose Multiplatform 1.11.0-alpha01
 * 的 JVM ABI 为验证基线。宿主升级编译器时应重新验证此布局及边界测试。
 * changed mask 每个容纳 10 个参数（含接收者），且至少存在一个；default mask 每个
 * 容纳 31 个值参数，不包含实例接收者。默认值的求值仍属于被调用方法，这里只定位编译器参数。
 * 仅从 descriptor 和本次转换输入的声明信息解析，不读取其他类资源。
 */
internal class ComposableCallSignature private constructor(
    val opcode: Int,
    val owner: String,
    val name: String,
    val descriptor: String,
    val isInterface: Boolean,
    val arguments: Array<Type>,
    val composerIndex: Int,
    private val declaringClass: ClassReader?
) {
    val returnType: Type = Type.getReturnType(descriptor)

    /**
     * 替换模式所需的 default mask 参数位置；无法确定静态接收者槽位时返回 null。
     * 追加模式不依赖 changed/default mask 布局，不调用此校验。
     */
    fun resolveDefaultMaskIndices(): IntRange? {
        val compilerArguments = (composerIndex + 1) until arguments.size
        check(compilerArguments.all { arguments[it] == Type.INT_TYPE }) {
            "UI Hook replacement requires Compose changed/default masks: $owner.$name$descriptor"
        }
        val receiverCount = implicitReceiverCount() ?: return null
        val changedCount = maxOf(
            1,
            (composerIndex + receiverCount + CHANGED_SLOTS_PER_MASK - 1) /
                CHANGED_SLOTS_PER_MASK
        )
        val defaultCount = compilerArguments.count() - changedCount
        // JVM descriptor 中的扩展/上下文接收者包含在 Composer 之前的参数中，
        // 因而这里只验证 default mask 数量的上限，不推测具体参数的默认位。
        val maxDefaultCount = (composerIndex + DEFAULT_SLOTS_PER_MASK - 1) /
            DEFAULT_SLOTS_PER_MASK
        check(defaultCount in 0..maxDefaultCount) {
            "UI Hook replacement requires Compose changed/default masks: $owner.$name$descriptor"
        }
        return (composerIndex + 1 + changedCount) until arguments.size
    }

    private fun implicitReceiverCount(): Int? {
        if (opcode != Opcodes.INVOKESTATIC) return 1
        // @JvmStatic 保留原实例接收者的 changed 槽位，但 JVM descriptor 不含该接收者。
        // 只有 10 个参数的边界会因此多出一个 mask；读取注解即可判定，无需加载/初始化类。
        if (composerIndex == 0 || composerIndex % CHANGED_SLOTS_PER_MASK != 0) return 0
        // 跨类调用没有当前输入中的声明信息；不借助类加载器猜测另一个版本的字节码。
        val reader = declaringClass ?: return null
        var methodFound = false
        var jvmStatic = false
        reader.accept(object : ClassVisitor(Opcodes.ASM9) {
            override fun visitMethod(
                access: Int,
                methodName: String,
                methodDescriptor: String,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor? {
                if (methodName != name || methodDescriptor != descriptor) return null
                methodFound = true
                return object : MethodVisitor(Opcodes.ASM9) {
                    override fun visitAnnotation(
                        annotationDescriptor: String,
                        visible: Boolean
                    ): AnnotationVisitor? {
                        if (annotationDescriptor == JVM_STATIC_DESCRIPTOR) jvmStatic = true
                        return null
                    }
                }
            }
        }, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        if (!methodFound) return null
        return if (jvmStatic) 1 else 0
    }

    companion object {
        const val COMPOSER_INTERNAL_NAME = "androidx/compose/runtime/Composer"
        const val COMPOSER_DESCRIPTOR = "L$COMPOSER_INTERNAL_NAME;"
        private const val JVM_STATIC_DESCRIPTOR = "Lkotlin/jvm/JvmStatic;"
        private const val CHANGED_SLOTS_PER_MASK = 10
        private const val DEFAULT_SLOTS_PER_MASK = 31

        fun resolve(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean,
            declaringClass: ClassReader?
        ): ComposableCallSignature {
            check(name != "<init>") { "UI Hook cannot wrap a constructor" }
            val arguments = Type.getArgumentTypes(descriptor)
            val composerIndex = findComposerIndex(arguments, "$owner.$name$descriptor")
            return ComposableCallSignature(
                opcode,
                owner,
                name,
                descriptor,
                isInterface,
                arguments,
                composerIndex,
                declaringClass
            )
        }

        fun findComposerIndex(arguments: Array<Type>, method: String): Int {
            val composers = arguments.indices.filter {
                arguments[it].descriptor == COMPOSER_DESCRIPTOR
            }
            check(composers.size == 1) {
                "UI Hook requires one Composer parameter: $method"
            }
            return composers.single()
        }
    }
}
