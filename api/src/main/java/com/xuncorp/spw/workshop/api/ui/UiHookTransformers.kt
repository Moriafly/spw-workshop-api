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
import com.xuncorp.spw.workshop.api.WorkshopApi
import com.xuncorp.spw.workshop.api.transform.BytecodeTransformExtensionPoint
import com.xuncorp.spw.workshop.api.transform.HostClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * 创建 UI Hook 字节码转换器，无需手写 Composer 参数位置与桥接调用指令
 *
 * 返回的转换器通过 [BytecodeTransformExtensionPoint] 提供给宿主，权限与生命周期沿用该拓展点
 * 转换器实例可复用，转换过程不修改输入数组，可在任意线程调用
 */
@SinceApi("1.19.0", "0.1.0-dev22")
object UiHookTransformers {
    /**
     * 将一个 Composable 调用替换为插件组件，未注册或未授权时执行原调用
     *
     * 原调用的参数表达式仍会执行，Composer 之前的业务参数通过 [UiHookContext.arguments] 传递
     * 仅支持返回 void 且具有唯一 Composer 参数的普通方法调用，插入点须处于有效组合中
     * 新分支使用平衡的组合分组，不主动触发重组；插件组件异常按普通 Composable 异常传播
     *
     * @param targetClassName 目标类二进制名
     * @param callOwner 被调用方法所属类二进制名
     * @param callMethodName 被调用方法的 JVM 名称
     * @param pluginId 插件 Manifest 的 Plugin-Id
     * @param hookId 本插件组件的 Hook ID
     * @param targetMethodName 目标方法 JVM 名称；null 表示在该类全部方法中寻找唯一调用，包含编译器生成的 lambda
     * @param targetMethodDescriptor 可选的目标方法完整 JVM descriptor
     * @param callMethodDescriptor 可选的被调用方法完整 JVM descriptor
     * @throws IllegalArgumentException 字符串参数为空白
     * @throws IllegalStateException 转换时目标缺失、不唯一、类名不符或调用签名不支持
     */
    @JvmStatic
    @JvmOverloads
    fun replaceCall(
        targetClassName: String,
        callOwner: String,
        callMethodName: String,
        pluginId: String,
        hookId: String,
        targetMethodName: String? = null,
        targetMethodDescriptor: String? = null,
        callMethodDescriptor: String? = null
    ): HostClassTransformer = invocationTransformer(
        targetClassName,
        callOwner,
        callMethodName,
        pluginId,
        hookId,
        targetMethodName,
        targetMethodDescriptor,
        callMethodDescriptor,
        contentArgumentIndex = null
    )

    /**
     * 包装 Composable 的普通 Function1 内容构建器，先构建原内容，再追加插件内容
     *
     * 适用于 PopupMenuScope.() -> Unit 等普通 DSL，不支持 @Composable 内容 lambda
     * 宿主调用前包装指定参数，插件的 [UiHookExtensionPoint.buildContent] 获得原构建器接收者
     * 其他业务参数可通过 [UiHookContext.arguments] 读取，例如原 onDismissRequest 回调
     * 未注册或未授权时使用原构建器，权限与生命周期沿用 UI Hook 注册
     *
     * @param contentArgumentIndex 内容构建器在被调用方法 JVM 参数中的索引，从 0 开始，不含实例接收者
     * @param targetMethodName null 表示在目标类全部方法中寻找唯一调用
     * @param targetMethodDescriptor 可选的目标方法完整 JVM descriptor
     * @param callMethodDescriptor 可选的被调用方法完整 JVM descriptor
     * @throws IllegalArgumentException 字符串参数为空白或内容索引为负数
     * @throws IllegalStateException 转换时目标缺失、不唯一、类名不符或调用签名不支持
     */
    @JvmStatic
    @JvmOverloads
    fun appendContent(
        targetClassName: String,
        callOwner: String,
        callMethodName: String,
        contentArgumentIndex: Int,
        pluginId: String,
        hookId: String,
        targetMethodName: String? = null,
        targetMethodDescriptor: String? = null,
        callMethodDescriptor: String? = null
    ): HostClassTransformer {
        require(contentArgumentIndex >= 0) { "contentArgumentIndex must not be negative" }
        return invocationTransformer(
            targetClassName,
            callOwner,
            callMethodName,
            pluginId,
            hookId,
            targetMethodName,
            targetMethodDescriptor,
            callMethodDescriptor,
            contentArgumentIndex
        )
    }

    private fun invocationTransformer(
        targetClassName: String,
        callOwner: String,
        callMethodName: String,
        pluginId: String,
        hookId: String,
        targetMethodName: String?,
        targetMethodDescriptor: String?,
        callMethodDescriptor: String?,
        contentArgumentIndex: Int?
    ): HostClassTransformer {
        require(targetClassName.isNotBlank()) { "targetClassName must not be blank" }
        require(callOwner.isNotBlank()) { "callOwner must not be blank" }
        require(callMethodName.isNotBlank()) { "callMethodName must not be blank" }
        require(pluginId.isNotBlank()) { "pluginId must not be blank" }
        require(hookId.isNotBlank()) { "hookId must not be blank" }
        require(targetMethodName == null || targetMethodName.isNotBlank()) { "targetMethodName must not be blank" }
        require(targetMethodDescriptor == null || targetMethodDescriptor.isNotBlank()) {
            "targetMethodDescriptor must not be blank"
        }
        require(callMethodDescriptor == null || callMethodDescriptor.isNotBlank()) {
            "callMethodDescriptor must not be blank"
        }
        return InvocationTransformer(
            targetClassName,
            callOwner.replace('.', '/'),
            callMethodName,
            pluginId,
            hookId,
            targetMethodName,
            targetMethodDescriptor,
            callMethodDescriptor,
            contentArgumentIndex
        )
    }

    private class InvocationTransformer(
        override val targetClassName: String,
        private val callOwner: String,
        private val callMethodName: String,
        private val pluginId: String,
        private val hookId: String,
        private val targetMethodName: String?,
        private val targetMethodDescriptor: String?,
        private val callMethodDescriptor: String?,
        private val contentArgumentIndex: Int?
    ) : HostClassTransformer {
        private val api: String = "com/xuncorp/spw/workshop/api/WorkshopApi"
        private val context: String = "com/xuncorp/spw/workshop/api/ui/UiHookContext"

        override fun transform(className: String, classfileBuffer: ByteArray): ByteArray {
            val reader = ClassReader(classfileBuffer)
            check(className == targetClassName && reader.className == targetClassName.replace('.', '/')) {
                "UI Hook class mismatch: expected $targetClassName, received $className / ${reader.className}"
            }
            val maxLocals = mutableMapOf<Pair<String, String>, Int>()
            reader.accept(object : ClassVisitor(Opcodes.ASM9) {
                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<out String>?
                ): MethodVisitor? {
                    if (!matchesMethod(name, descriptor)) return null
                    return object : MethodVisitor(Opcodes.ASM9) {
                        override fun visitMaxs(maxStack: Int, locals: Int) {
                            maxLocals[name to descriptor] = locals
                        }
                    }
                }
            }, ClassReader.SKIP_FRAMES)
            val writer = object : ClassWriter(reader, ClassWriter.COMPUTE_FRAMES) {
                override fun getClassLoader(): ClassLoader = WorkshopApi::class.java.classLoader
            }
            var methods = 0
            var calls = 0
            reader.accept(object : ClassVisitor(Opcodes.ASM9, writer) {
                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<out String>?
                ): MethodVisitor {
                    val output = super.visitMethod(
                        access,
                        name,
                        descriptor,
                        signature,
                        exceptions
                    )
                    if (!matchesMethod(name, descriptor)) return output
                    methods++
                    val localBase = maxLocals[name to descriptor]
                    return object : MethodVisitor(Opcodes.ASM9, output) {
                        override fun visitMethodInsn(
                            opcode: Int,
                            owner: String,
                            name: String,
                            descriptor: String,
                            isInterface: Boolean
                        ) {
                            if (owner == callOwner && name == callMethodName &&
                                (callMethodDescriptor == null || descriptor == callMethodDescriptor)
                            ) {
                                calls++
                                check(calls == 1) { "UI Hook call is ambiguous: $className -> $callOwner.$callMethodName" }
                                emitInvocation(
                                    output,
                                    opcode,
                                    owner,
                                    name,
                                    descriptor,
                                    isInterface,
                                    checkNotNull(localBase)
                                )
                            } else {
                                super.visitMethodInsn(
                                    opcode,
                                    owner,
                                    name,
                                    descriptor,
                                    isInterface
                                )
                            }
                        }
                    }
                }
            }, ClassReader.SKIP_FRAMES)
            check(targetMethodName == null || methods == 1) {
                "UI Hook method missing or ambiguous: $className.$targetMethodName ($methods matches)"
            }
            check(calls == 1) { "UI Hook call not found: $className -> $callOwner.$callMethodName" }
            return writer.toByteArray()
        }

        private fun matchesMethod(name: String, descriptor: String): Boolean =
            (targetMethodName == null || name == targetMethodName) &&
                (targetMethodDescriptor == null || descriptor == targetMethodDescriptor)

        private fun emitInvocation(
            visitor: MethodVisitor,
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean,
            localBase: Int
        ): Unit = with(visitor) {
            val arguments = Type.getArgumentTypes(descriptor)
            val composers = arguments.indices.filter { arguments[it].descriptor == "Landroidx/compose/runtime/Composer;" }
            check(composers.size == 1) { "UI Hook requires one Composer parameter: $owner.$name$descriptor" }
            val composerIndex = composers.single()
            check(name != "<init>") { "UI Hook cannot wrap a constructor" }
            if (contentArgumentIndex == null) {
                check(Type.getReturnType(descriptor).sort == Type.VOID) { "UI Hook replacement requires a void call" }
            } else {
                check(contentArgumentIndex < composerIndex &&
                    arguments[contentArgumentIndex].descriptor == "Lkotlin/jvm/functions/Function1;"
                ) { "UI Hook content must be a Function1 business parameter: $owner.$name$descriptor" }
            }
            var nextLocal = localBase
            val locals = arguments.map { argument ->
                nextLocal.also { nextLocal += argument.size }
            }
            for (index in arguments.indices.reversed()) {
                visitVarInsn(arguments[index].getOpcode(Opcodes.ISTORE), locals[index])
            }
            val receiverLocal = if (opcode == Opcodes.INVOKESTATIC) null else nextLocal
            receiverLocal?.let { visitVarInsn(Opcodes.ASTORE, it) }
            if (contentArgumentIndex != null) {
                emitUi(visitor)
                visitLdcInsn(pluginId)
                visitLdcInsn(hookId)
                visitVarInsn(Opcodes.ALOAD, locals[contentArgumentIndex])
                emitArguments(visitor, arguments, locals, composerIndex)
                visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    "$api\$Ui",
                    "appendHookContent",
                    "(Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;[Ljava/lang/Object;)Lkotlin/jvm/functions/Function1;",
                    true
                )
                visitVarInsn(Opcodes.ASTORE, locals[contentArgumentIndex])
                emitOriginal(
                    visitor,
                    opcode,
                    owner,
                    name,
                    descriptor,
                    isInterface,
                    arguments,
                    locals,
                    receiverLocal
                )
            } else {
                val original = Label()
                val end = Label()
                visitVarInsn(Opcodes.ALOAD, locals[composerIndex])
                visitLdcInsn("$pluginId/$hookId/$owner.$name".hashCode())
                visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    "androidx/compose/runtime/Composer",
                    "startReplaceGroup",
                    "(I)V",
                    true
                )
                emitUi(visitor)
                visitLdcInsn(pluginId)
                visitLdcInsn(hookId)
                visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    "$api\$Ui",
                    "hasHook",
                    "(Ljava/lang/String;Ljava/lang/String;)Z",
                    true
                )
                visitJumpInsn(Opcodes.IFEQ, original)
                emitUi(visitor)
                visitLdcInsn(pluginId)
                visitLdcInsn(hookId)
                visitTypeInsn(Opcodes.NEW, context)
                visitInsn(Opcodes.DUP)
                emitArguments(visitor, arguments, locals, composerIndex)
                visitInsn(Opcodes.ACONST_NULL)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    context,
                    "<init>",
                    "([Ljava/lang/Object;Ljava/lang/Object;)V",
                    false
                )
                visitVarInsn(Opcodes.ALOAD, locals[composerIndex])
                visitInsn(Opcodes.ICONST_0)
                visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    "$api\$Ui",
                    "renderHook",
                    "(Ljava/lang/String;Ljava/lang/String;L$context;Landroidx/compose/runtime/Composer;I)V",
                    true
                )
                visitJumpInsn(Opcodes.GOTO, end)
                visitLabel(original)
                emitOriginal(
                    visitor,
                    opcode,
                    owner,
                    name,
                    descriptor,
                    isInterface,
                    arguments,
                    locals,
                    receiverLocal
                )
                visitLabel(end)
                visitVarInsn(Opcodes.ALOAD, locals[composerIndex])
                visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    "androidx/compose/runtime/Composer",
                    "endReplaceGroup",
                    "()V",
                    true
                )
            }
        }

        private fun emitUi(visitor: MethodVisitor) {
            visitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                api,
                "ui",
                "()L$api\$Ui;",
                true
            )
        }

        private fun emitArguments(
            visitor: MethodVisitor,
            arguments: Array<Type>,
            locals: List<Int>,
            count: Int
        ): Unit = with(visitor) {
            visitLdcInsn(count)
            visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")
            for (index in 0 until count) {
                visitInsn(Opcodes.DUP)
                visitLdcInsn(index)
                val type = arguments[index]
                visitVarInsn(type.getOpcode(Opcodes.ILOAD), locals[index])
                val boxed = when (type.sort) {
                    Type.BOOLEAN -> "Boolean"
                    Type.BYTE -> "Byte"
                    Type.CHAR -> "Character"
                    Type.SHORT -> "Short"
                    Type.INT -> "Integer"
                    Type.FLOAT -> "Float"
                    Type.LONG -> "Long"
                    Type.DOUBLE -> "Double"
                    else -> null
                }
                if (boxed != null) {
                    visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/$boxed",
                        "valueOf",
                        "(${type.descriptor})Ljava/lang/$boxed;",
                        false
                    )
                }
                visitInsn(Opcodes.AASTORE)
            }
        }

        private fun emitOriginal(
            visitor: MethodVisitor,
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean,
            arguments: Array<Type>,
            locals: List<Int>,
            receiverLocal: Int?
        ): Unit = with(visitor) {
            receiverLocal?.let { visitVarInsn(Opcodes.ALOAD, it) }
            arguments.indices.forEach { index ->
                visitVarInsn(arguments[index].getOpcode(Opcodes.ILOAD), locals[index])
            }
            visitMethodInsn(
                opcode,
                owner,
                name,
                descriptor,
                isInterface
            )
        }
    }

    /**
     * 在目标 Composable 中指定方法调用之后插入 [WorkshopApi.Ui.renderHook]
     *
     * 自动定位当前方法的 Composer 参数，兼容实例方法、静态方法以及 long/double 参数
     * 保留原有控制流与组合分组，仅在原调用正常返回后执行 Hook
     * 插入点仍需由插件选择，须位于有效组合的执行分支内，且 Composer 参数在该位置仍有效
     * 本方法不验证组合协议或布局作用域，也不主动触发重组
     *
     * 目标方法与其中的调用都必须恰好匹配一次；有重载时可用 JVM descriptor 限定
     * 宿主升级后目标缺失或不唯一时转换失败，由宿主记录错误，不返回部分修改的字节码
     *
     * @param targetClassName 目标类的二进制名，例如 com.example.ScreenKt
     * @param targetMethodName 目标方法的 JVM 名称，例如 ScreenContent
     * @param callOwner 被调用方法所属类的二进制名，例如 androidx.compose.foundation.lazy.LazyDslKt
     * @param callMethodName 被调用方法的 JVM 名称，例如 LazyColumn
     * @param pluginId 插件 Manifest 中的 Plugin-Id
     * @param hookId 本插件 [UiHookExtensionPoint.hookId]
     * @param targetMethodDescriptor 目标方法的完整 JVM descriptor，含 Composer 等编译器参数；null 表示只按名称匹配
     * @param callMethodDescriptor 被调用方法的完整 JVM descriptor；null 表示只按所属类与名称匹配
     * @return 应在拓展实例内持有的转换器，避免每次收集时创建新实例
     * @throws IllegalArgumentException 必填字符串或非 null 的 descriptor 为空白
     * @throws IllegalStateException 转换时类名不符、匹配不唯一，或目标方法没有唯一的 Composer 参数
     */
    @JvmStatic
    @JvmOverloads
    fun afterCall(
        targetClassName: String,
        targetMethodName: String,
        callOwner: String,
        callMethodName: String,
        pluginId: String,
        hookId: String,
        targetMethodDescriptor: String? = null,
        callMethodDescriptor: String? = null
    ): HostClassTransformer {
        require(targetClassName.isNotBlank()) { "targetClassName must not be blank" }
        require(targetMethodName.isNotBlank()) { "targetMethodName must not be blank" }
        require(callOwner.isNotBlank()) { "callOwner must not be blank" }
        require(callMethodName.isNotBlank()) { "callMethodName must not be blank" }
        require(pluginId.isNotBlank()) { "pluginId must not be blank" }
        require(hookId.isNotBlank()) { "hookId must not be blank" }
        require(targetMethodDescriptor == null || targetMethodDescriptor.isNotBlank()) {
            "targetMethodDescriptor must not be blank"
        }
        require(callMethodDescriptor == null || callMethodDescriptor.isNotBlank()) {
            "callMethodDescriptor must not be blank"
        }
        return AfterCallTransformer(
            targetClassName = targetClassName,
            targetMethodName = targetMethodName,
            callOwner = callOwner.replace('.', '/'),
            callMethodName = callMethodName,
            pluginId = pluginId,
            hookId = hookId,
            targetMethodDescriptor = targetMethodDescriptor,
            callMethodDescriptor = callMethodDescriptor
        )
    }

    private class AfterCallTransformer(
        override val targetClassName: String,
        private val targetMethodName: String,
        private val callOwner: String,
        private val callMethodName: String,
        private val pluginId: String,
        private val hookId: String,
        private val targetMethodDescriptor: String?,
        private val callMethodDescriptor: String?
    ) : HostClassTransformer {
        override fun transform(className: String, classfileBuffer: ByteArray): ByteArray {
            val reader = ClassReader(classfileBuffer)
            check(className == targetClassName && reader.className == targetClassName.replace('.', '/')) {
                "UI Hook class mismatch: expected $targetClassName, received $className / ${reader.className}"
            }
            val writer = ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
            var methods = 0
            var calls = 0
            reader.accept(object : ClassVisitor(Opcodes.ASM9, writer) {
                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<out String>?
                ): MethodVisitor {
                    val original = super.visitMethod(
                        access,
                        name,
                        descriptor,
                        signature,
                        exceptions
                    )
                    if (name != targetMethodName ||
                        (targetMethodDescriptor != null && descriptor != targetMethodDescriptor)
                    ) return original
                    methods++
                    check(methods == 1) {
                        "UI Hook method is ambiguous: $className.$targetMethodName; specify targetMethodDescriptor"
                    }
                    val composerLocal = composerLocal(access, descriptor)
                    return object : MethodVisitor(Opcodes.ASM9, original) {
                        override fun visitMethodInsn(
                            opcode: Int,
                            owner: String,
                            name: String,
                            descriptor: String,
                            isInterface: Boolean
                        ) {
                            super.visitMethodInsn(
                                opcode,
                                owner,
                                name,
                                descriptor,
                                isInterface
                            )
                            if (owner != callOwner || name != callMethodName ||
                                (callMethodDescriptor != null && descriptor != callMethodDescriptor)
                            ) return
                            calls++
                            check(calls == 1) {
                                "UI Hook call is ambiguous: $className.$targetMethodName -> $callOwner.$callMethodName"
                            }
                            // 写入下游 visitor，避免注入的调用再次触发当前匹配器
                            emitHook(original, composerLocal)
                        }
                    }
                }
            }, 0)
            check(methods == 1) { "UI Hook method not found: $className.$targetMethodName" }
            check(calls == 1) {
                "UI Hook call not found: $className.$targetMethodName -> $callOwner.$callMethodName"
            }
            return writer.toByteArray()
        }

        private fun composerLocal(access: Int, descriptor: String): Int {
            val arguments = Type.getArgumentTypes(descriptor)
            val composerArguments = arguments.indices.filter {
                arguments[it].descriptor == "Landroidx/compose/runtime/Composer;"
            }
            check(composerArguments.size == 1) {
                "UI Hook requires one Composer parameter: $targetClassName.$targetMethodName$descriptor"
            }
            val receiverSize = if (access and Opcodes.ACC_STATIC == 0) 1 else 0
            return receiverSize + arguments.take(composerArguments.single()).sumOf { it.size }
        }

        private fun emitHook(visitor: MethodVisitor, composerLocal: Int): Unit = with(visitor) {
            val api = "com/xuncorp/spw/workshop/api/WorkshopApi"
            visitMethodInsn(
                Opcodes.INVOKESTATIC,
                api,
                "ui",
                "()L$api\$Ui;",
                true
            )
            visitLdcInsn(pluginId)
            visitLdcInsn(hookId)
            visitVarInsn(Opcodes.ALOAD, composerLocal)
            visitInsn(Opcodes.ICONST_0)
            visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                "$api\$Ui",
                "renderHook",
                "(Ljava/lang/String;Ljava/lang/String;Landroidx/compose/runtime/Composer;I)V",
                true
            )
        }
    }
}
