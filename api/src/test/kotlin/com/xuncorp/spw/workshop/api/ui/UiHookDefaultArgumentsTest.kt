@file:OptIn(androidx.compose.runtime.InternalComposeApi::class)

package com.xuncorp.spw.workshop.api.ui

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.currentComposer
import com.xuncorp.spw.workshop.api.WorkshopApi
import com.xuncorp.spw.workshop.api.transform.HostClassTransformer
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.lang.reflect.Proxy
import kotlin.coroutines.EmptyCoroutineContext

object UiHookDefaultArgumentsProbe {
    var originalContentCalls: Int = 0
    var originalArguments: List<Any?>? = null
    var defaultEvaluations: Int = 0

    fun defaultText(): String {
        defaultEvaluations++
        return "host-default"
    }
}

class UiHookDefaultArgumentsTest {
    private var previousApi: WorkshopApi? = null
    private lateinit var hooks: RecordingUi

    @Before
    fun setUp() {
        previousApi = runCatching { WorkshopApi.instance }.getOrNull()
        hooks = RecordingUi()
        installUi(hooks)
        UiHookDefaultArgumentsProbe.originalContentCalls = 0
        UiHookDefaultArgumentsProbe.originalArguments = null
        UiHookDefaultArgumentsProbe.defaultEvaluations = 0
    }

    @After
    fun restoreApi() {
        // 宿主注入入口是全局 lateinit 字段，恢复包括原来尚未初始化的状态。
        WorkshopApi.Companion::class.java.getField("instance").set(null, previousApi)
    }

    @Test
    fun omittedContentUsesHostDefaultWithoutCallingBridge() {
        compose(append("DefaultContent", "CallDefaultContent", 0), "CallDefaultContent")

        assertEquals(1, UiHookDefaultArgumentsProbe.originalContentCalls)
        assertEquals(0, hooks.appendCalls)
        assertEquals(0, hooks.appendedContentCalls)
    }

    @Test
    fun omittedContentWorksWithLegacyUiImplementation() {
        installUi(object : WorkshopApi.Ui {
            override fun toast(text: String, type: WorkshopApi.Ui.ToastType) {}
        })

        compose(append("DefaultContent", "CallDefaultContent", 0), "CallDefaultContent")

        assertEquals(1, UiHookDefaultArgumentsProbe.originalContentCalls)
    }

    @Test
    fun explicitlyNullContentKeepsNullableCalleeSemantics() {
        compose(append("NullableContent", "CallNullContent", 0), "CallNullContent")

        assertEquals(listOf<Any?>(null), UiHookDefaultArgumentsProbe.originalArguments)
        assertEquals(0, hooks.appendCalls)
    }

    @Test
    fun explicitContentCanBeAppendedWhileOtherArgumentsUseDefaults() {
        compose(append("OptionalHeader", "CallExplicitContent", 1), "CallExplicitContent")

        assertEquals(listOf("host-header"), UiHookDefaultArgumentsProbe.originalArguments)
        assertEquals(1, UiHookDefaultArgumentsProbe.originalContentCalls)
        assertEquals(1, hooks.appendCalls)
        assertEquals(1, hooks.appendedContentCalls)
    }

    @Test
    fun appendPreservesNonstandardTailAndReturnValue() {
        compose(append("NonstandardContent", "CallNonstandardContent", 0), "CallNonstandardContent")

        assertEquals(listOf("original-return"), UiHookDefaultArgumentsProbe.originalArguments)
        assertEquals(1, UiHookDefaultArgumentsProbe.originalContentCalls)
        assertEquals(1, hooks.appendCalls)
        assertEquals(1, hooks.appendedContentCalls)
    }

    @Test
    fun afterCallPreservesReturnValueAndRunsAfterOriginalContent() {
        compose(after("NonstandardContent", "CallNonstandardContent"), "CallNonstandardContent")

        assertEquals(listOf("original-return"), UiHookDefaultArgumentsProbe.originalArguments)
        assertEquals(1, hooks.originalContentCallsAtRender)
        assertEquals(emptyList<Any?>(), hooks.renderedArguments)
    }

    @Test
    fun replacementRejectsAmbiguousCallsAcrossMethods() {
        val transformer = UiHookTransformers.replaceCall(
            FIXTURE_CLASS_NAME,
            MEMBER_CLASS_NAME,
            "Content",
            "test-plugin",
            "test-hook"
        )

        val error = assertThrows(IllegalStateException::class.java) {
            transformer.transform(FIXTURE_CLASS_NAME, fixtureBytes(FIXTURE_CLASS_NAME))
        }

        assertTrue(error.message.orEmpty().contains("call is ambiguous"))
    }

    @Test
    fun afterCallRequiresMatchingCallerDescriptor() {
        val transformer = after(
            "NonstandardContent",
            "CallNonstandardContent",
            targetDescriptor = "(Landroidx/compose/runtime/Composer;II)V"
        )

        val error = assertThrows(IllegalStateException::class.java) {
            transformer.transform(FIXTURE_CLASS_NAME, fixtureBytes(FIXTURE_CLASS_NAME))
        }

        assertTrue(error.message.orEmpty().contains("method not found"))
    }

    @Test
    fun afterCallRequiresMatchingCalleeDescriptor() {
        val transformer = after(
            "NonstandardContent",
            "CallNonstandardContent",
            callDescriptor = "(Landroidx/compose/runtime/Composer;)V"
        )

        val error = assertThrows(IllegalStateException::class.java) {
            transformer.transform(FIXTURE_CLASS_NAME, fixtureBytes(FIXTURE_CLASS_NAME))
        }

        assertTrue(error.message.orEmpty().contains("call not found"))
    }

    @Test
    fun invocationAndAfterCallRejectMismatchedClassNames() {
        val transformers = listOf(
            append("NonstandardContent", "CallNonstandardContent", 0),
            after("NonstandardContent", "CallNonstandardContent")
        )
        val input = fixtureBytes(FIXTURE_CLASS_NAME)
        for (transformer in transformers) {
            val error = assertThrows(IllegalStateException::class.java) {
                transformer.transform("different.HostClass", input)
            }
            assertTrue(error.message.orEmpty().contains("class mismatch"))
        }
    }

    @Test
    fun replacementPreservesDefaultExpressionsAndWidePrimitiveArguments() {
        compose(replace("DefaultArguments", "CallDefaultArguments"), "CallDefaultArguments")

        assertEquals(
            listOf("host-default", true, 42L, 1.5),
            UiHookDefaultArgumentsProbe.originalArguments
        )
        assertEquals(1, UiHookDefaultArgumentsProbe.defaultEvaluations)
        assertEquals(0, hooks.hasHookCalls)
        assertNull(hooks.renderedArguments)
    }

    @Test
    fun replacementPassesExplicitNullAndPrimitiveValuesToHook() {
        compose(replace("DefaultArguments", "CallExplicitArguments"), "CallExplicitArguments")

        assertEquals(listOf(null, false, 7L, 2.5), hooks.renderedArguments)
        assertEquals(0, UiHookDefaultArgumentsProbe.defaultEvaluations)
        assertNull(UiHookDefaultArgumentsProbe.originalArguments)
    }

    @Test
    fun memberDefaultFallsBackAfterTwoChangedMasksAndWideLocals() {
        compose(
            replace("Content", "CallMemberDefault", MEMBER_CLASS_NAME),
            "CallMemberDefault"
        )

        assertEquals(listOf(42L, 3.0), UiHookDefaultArgumentsProbe.originalArguments)
        assertEquals(0, hooks.hasHookCalls)
    }

    @Test
    fun memberChangedMasksDoNotDisableExplicitReplacement() {
        compose(
            replace("Content", "CallMemberExplicit", MEMBER_CLASS_NAME),
            "CallMemberExplicit"
        )

        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6, 7, 42L, 4.5), hooks.renderedArguments)
        assertNull(UiHookDefaultArgumentsProbe.originalArguments)
    }

    @Test
    fun jvmStaticDefaultFallsBackWithImplicitReceiverSlot() {
        compose(
            replace("Content", "CallStaticDefault", STATIC_CLASS_NAME),
            "CallStaticDefault"
        )

        assertEquals(listOf(42L, 3.0), UiHookDefaultArgumentsProbe.originalArguments)
        assertEquals(0, hooks.hasHookCalls)
    }

    @Test
    fun externalStaticReceiverAmbiguityPreservesExplicitCall() {
        compose(
            replace("Content", "CallStaticExplicit", STATIC_CLASS_NAME),
            "CallStaticExplicit"
        )

        assertEquals(listOf(42L, 4.5), UiHookDefaultArgumentsProbe.originalArguments)
        assertEquals(0, hooks.hasHookCalls)
        assertNull(hooks.renderedArguments)
    }

    @Test
    fun ownStaticDeclarationPreservesDefaultEvaluation() {
        compose(
            replace("Content", "CallOwnDefault", STATIC_CLASS_NAME, STATIC_CLASS_NAME),
            "CallOwnDefault"
        )

        assertEquals(listOf(42L, 3.0), UiHookDefaultArgumentsProbe.originalArguments)
        assertEquals(0, hooks.hasHookCalls)
    }

    @Test
    fun ownStaticDeclarationSeparatesChangedAndDefaultMasks() {
        compose(
            replace("Content", "CallOwnExplicit", STATIC_CLASS_NAME, STATIC_CLASS_NAME),
            "CallOwnExplicit"
        )

        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6, 7, 42L, 4.5), hooks.renderedArguments)
        assertNull(UiHookDefaultArgumentsProbe.originalArguments)
    }

    @Test
    fun unavailableStaticOwnerTransformsDeterministicallyWithoutBridge() {
        val caller = "CallStaticExplicit"
        val input = fixtureBytes(FIXTURE_CLASS_NAME)
        val reader = ClassReader(input)
        val writer = ClassWriter(reader, 0)
        val missingOwner = "unavailable/host/StaticContent"
        reader.accept(object : ClassVisitor(Opcodes.ASM9, writer) {
            override fun visitMethod(
                access: Int,
                name: String,
                descriptor: String,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor {
                val output = super.visitMethod(access, name, descriptor, signature, exceptions)
                if (name != caller) return output
                return object : MethodVisitor(Opcodes.ASM9, output) {
                    override fun visitMethodInsn(
                        opcode: Int,
                        owner: String,
                        name: String,
                        descriptor: String,
                        isInterface: Boolean
                    ) {
                        val rewrittenOwner = if (owner == STATIC_CLASS_NAME.replace('.', '/')) {
                            missingOwner
                        } else owner
                        super.visitMethodInsn(opcode, rewrittenOwner, name, descriptor, isInterface)
                    }
                }
            }
        }, 0)
        val unavailableInput = writer.toByteArray()
        val transformer = replace("Content", caller, missingOwner.replace('/', '.'))

        val transformed = checkNotNull(transformer.transform(FIXTURE_CLASS_NAME, unavailableInput))
        val repeated = checkNotNull(transformer.transform(FIXTURE_CLASS_NAME, unavailableInput))

        assertArrayEquals(transformed, repeated)
        // 回退须保留全部调用指令，包括原 callee，而不是只跳过 hasHook 查询。
        assertEquals(invocations(unavailableInput, caller), invocations(transformed, caller))
    }

    @Test
    fun thirtyOneMemberParametersUseOneDefaultMask() {
        compose(
            replace("Content", "CallMemberBoundaryDefault", MEMBER_BOUNDARY_CLASS_NAME),
            "CallMemberBoundaryDefault"
        )

        assertEquals(listOf(0, 130), UiHookDefaultArgumentsProbe.originalArguments)
        assertEquals(0, hooks.hasHookCalls)
    }

    @Test
    fun thirtyOneExplicitMemberParametersCanBeReplaced() {
        compose(
            replace("Content", "CallMemberBoundaryExplicit", MEMBER_BOUNDARY_CLASS_NAME),
            "CallMemberBoundaryExplicit"
        )

        assertEquals((0..30).toList(), hooks.renderedArguments)
        assertNull(UiHookDefaultArgumentsProbe.originalArguments)
    }

    @Test
    fun firstDefaultMaskFallsBackWhenSecondMaskIsZero() {
        compose(replace("MultipleMasks", "CallFirstMaskDefault"), "CallFirstMaskDefault")

        assertEquals(listOf(100, 31), UiHookDefaultArgumentsProbe.originalArguments)
        assertEquals(0, hooks.hasHookCalls)
    }

    @Test
    fun secondDefaultMaskFallsBackWhenFirstMaskIsZero() {
        compose(replace("MultipleMasks", "CallSecondMaskDefault"), "CallSecondMaskDefault")

        assertEquals(listOf(0, 131), UiHookDefaultArgumentsProbe.originalArguments)
        assertEquals(0, hooks.hasHookCalls)
    }

    @Test
    fun multipleChangedMasksDoNotDisableExplicitReplacement() {
        compose(
            replace("MultipleMasks", "CallMultipleMasksExplicit"),
            "CallMultipleMasksExplicit"
        )

        assertEquals((0..31).toList(), hooks.renderedArguments)
        assertNull(UiHookDefaultArgumentsProbe.originalArguments)
    }

    private fun installUi(ui: WorkshopApi.Ui) {
        WorkshopApi.instance = Proxy.newProxyInstance(
            WorkshopApi::class.java.classLoader,
            arrayOf(WorkshopApi::class.java)
        ) { _, method, _ ->
            check(method.name == "getUi") { "Unexpected API call: ${method.name}" }
            ui
        } as WorkshopApi
    }

    private fun append(method: String, caller: String, contentIndex: Int): HostClassTransformer =
        UiHookTransformers.appendContent(
            FIXTURE_CLASS_NAME,
            FIXTURE_CLASS_NAME,
            method,
            contentIndex,
            "test-plugin",
            "test-hook",
            caller
        )

    private fun replace(
        method: String,
        caller: String,
        owner: String = FIXTURE_CLASS_NAME,
        targetClassName: String = FIXTURE_CLASS_NAME
    ): HostClassTransformer = UiHookTransformers.replaceCall(
        targetClassName,
        owner,
        method,
        "test-plugin",
        "test-hook",
        caller
    )

    private fun after(
        method: String,
        caller: String,
        targetDescriptor: String? = null,
        callDescriptor: String? = null
    ): HostClassTransformer = UiHookTransformers.afterCall(
        FIXTURE_CLASS_NAME,
        caller,
        FIXTURE_CLASS_NAME,
        method,
        "test-plugin",
        "test-hook",
        targetDescriptor,
        callDescriptor
    )

    private fun compose(transformer: HostClassTransformer, caller: String) {
        val className = transformer.targetClassName
        val input = fixtureBytes(className)
        val transformed = checkNotNull(transformer.transform(className, input))
        val fixtureClass = object : ClassLoader(javaClass.classLoader) {
            fun defineFixture(): Class<*> =
                defineClass(className, transformed, 0, transformed.size)
        }.defineFixture()
        val method = fixtureClass.getMethod(
            caller,
            Composer::class.java,
            Int::class.javaPrimitiveType
        )
        val recomposer = Recomposer(EmptyCoroutineContext)
        val composition = Composition(EmptyApplier(), recomposer)
        try {
            composition.setContent { method.invoke(null, currentComposer, 0) }
        } finally {
            composition.dispose()
            recomposer.close()
        }
    }

    private fun fixtureBytes(className: String): ByteArray =
        javaClass.classLoader.getResourceAsStream(className.replace('.', '/') + ".class")
            .let { stream -> checkNotNull(stream) { "Fixture bytecode not found: $className" } }
            .use { it.readBytes() }

    private fun invocations(bytecode: ByteArray, method: String): List<String> {
        val calls = mutableListOf<String>()
        ClassReader(bytecode).accept(object : ClassVisitor(Opcodes.ASM9) {
            override fun visitMethod(
                access: Int,
                name: String,
                descriptor: String,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor? {
                if (name != method) return null
                return object : MethodVisitor(Opcodes.ASM9) {
                    override fun visitMethodInsn(
                        opcode: Int,
                        owner: String,
                        name: String,
                        descriptor: String,
                        isInterface: Boolean
                    ) {
                        calls += "$opcode $owner.$name$descriptor $isInterface"
                    }
                }
            }
        }, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        return calls
    }

    private class RecordingUi : WorkshopApi.Ui {
        var appendCalls = 0
        var appendedContentCalls = 0
        var hasHookCalls = 0
        var renderedArguments: List<Any?>? = null
        var originalContentCallsAtRender: Int? = null

        override fun hasHook(pluginId: String, hookId: String): Boolean {
            hasHookCalls++
            return true
        }

        @Composable
        override fun renderHook(pluginId: String, hookId: String) {
            renderHook(pluginId, hookId, UiHookContext.Empty)
        }

        @Composable
        override fun renderHook(pluginId: String, hookId: String, context: UiHookContext) {
            renderedArguments = context.arguments
            originalContentCallsAtRender = UiHookDefaultArgumentsProbe.originalContentCalls
        }

        override fun appendHookContent(
            pluginId: String,
            hookId: String,
            content: (Any?) -> Unit,
            arguments: Array<out Any?>
        ): (Any?) -> Unit {
            appendCalls++
            return { scope ->
                content(scope)
                appendedContentCalls++
            }
        }

        override fun toast(text: String, type: WorkshopApi.Ui.ToastType) {}
    }

    private class EmptyApplier : AbstractApplier<Unit>(Unit) {
        override fun insertTopDown(index: Int, instance: Unit) {}
        override fun insertBottomUp(index: Int, instance: Unit) {}
        override fun remove(index: Int, count: Int) {}
        override fun move(from: Int, to: Int, count: Int) {}
        override fun onClear() {}
    }

    private companion object {
        const val FIXTURE_PACKAGE = "com.xuncorp.spw.workshop.api.ui.fixtures"
        const val FIXTURE_CLASS_NAME = "$FIXTURE_PACKAGE.UiHookDefaultArgumentsFixtureKt"
        const val MEMBER_CLASS_NAME = "$FIXTURE_PACKAGE.MemberContent"
        const val MEMBER_BOUNDARY_CLASS_NAME = "$FIXTURE_PACKAGE.MemberBoundaryContent"
        const val STATIC_CLASS_NAME = "$FIXTURE_PACKAGE.StaticContent"
    }
}
