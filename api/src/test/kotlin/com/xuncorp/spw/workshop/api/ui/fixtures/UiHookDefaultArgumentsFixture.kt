@file:Suppress("UNUSED_PARAMETER")
@file:OptIn(androidx.compose.runtime.InternalComposeApi::class)

package com.xuncorp.spw.workshop.api.ui.fixtures

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.currentComposer
import com.xuncorp.spw.workshop.api.ui.UiHookDefaultArgumentsProbe

@Composable
fun DefaultContent(content: (Any?) -> Unit = {
    UiHookDefaultArgumentsProbe.originalContentCalls++
}) {
    content(Unit)
}

@Composable
fun CallDefaultContent() {
    DefaultContent()
}

@Composable
fun NullableContent(content: ((Any?) -> Unit)?) {
    UiHookDefaultArgumentsProbe.originalArguments = listOf(content)
}

@Composable
fun CallNullContent() {
    NullableContent(null)
}

@Composable
fun OptionalHeader(
    header: String = "host-header",
    content: (Any?) -> Unit
) {
    UiHookDefaultArgumentsProbe.originalArguments = listOf(header)
    content(Unit)
}

@Composable
fun CallExplicitContent() {
    OptionalHeader {
        UiHookDefaultArgumentsProbe.originalContentCalls++
    }
}

// 追加模式原有契约仅要求唯一 Composer 和普通 Function1，不要求标准编译器尾参数。
fun NonstandardContent(content: (Any?) -> Unit, composer: Composer, marker: String): String {
    content(Unit)
    return marker
}

@Composable
fun CallNonstandardContent() {
    val result = NonstandardContent(
        { UiHookDefaultArgumentsProbe.originalContentCalls++ },
        currentComposer,
        "original-return"
    )
    UiHookDefaultArgumentsProbe.originalArguments = listOf(result)
}

@Composable
fun DefaultArguments(
    text: String? = UiHookDefaultArgumentsProbe.defaultText(),
    enabled: Boolean = true,
    position: Long = 42L,
    scale: Double = 1.5
) {
    UiHookDefaultArgumentsProbe.originalArguments = listOf(text, enabled, position, scale)
}

@Composable
fun CallDefaultArguments() {
    DefaultArguments()
}

@Composable
fun CallExplicitArguments() {
    DefaultArguments(null, false, 7L, 2.5)
}

class MemberContent {
    @Composable
    fun Content(
        p0: Int,
        p1: Int,
        p2: Int,
        p3: Int,
        p4: Int,
        p5: Int,
        p6: Int,
        p7: Int,
        position: Long,
        scale: Double = 3.0
    ) {
        UiHookDefaultArgumentsProbe.originalArguments = listOf(position, scale)
    }
}

@Composable
fun CallMemberDefault() {
    MemberContent().Content(0, 1, 2, 3, 4, 5, 6, 7, 42L)
}

@Composable
fun CallMemberExplicit() {
    MemberContent().Content(0, 1, 2, 3, 4, 5, 6, 7, 42L, 4.5)
}

object StaticContent {
    @JvmStatic
    @Composable
    fun Content(
        p0: Int,
        p1: Int,
        p2: Int,
        p3: Int,
        p4: Int,
        p5: Int,
        p6: Int,
        p7: Int,
        position: Long,
        scale: Double = 3.0
    ) {
        UiHookDefaultArgumentsProbe.originalArguments = listOf(position, scale)
    }

    @JvmStatic
    @Composable
    fun CallOwnDefault() {
        Content(0, 1, 2, 3, 4, 5, 6, 7, 42L)
    }

    @JvmStatic
    @Composable
    fun CallOwnExplicit() {
        Content(0, 1, 2, 3, 4, 5, 6, 7, 42L, 4.5)
    }
}

@Composable
fun CallStaticDefault() {
    StaticContent.Content(0, 1, 2, 3, 4, 5, 6, 7, 42L)
}

@Composable
fun CallStaticExplicit() {
    StaticContent.Content(0, 1, 2, 3, 4, 5, 6, 7, 42L, 4.5)
}

// 31 个值参数加实例接收者：4 个 changed masks，但仍只有 1 个 default mask。
class MemberBoundaryContent {
    @Composable
    fun Content(
        p00: Int,
        p01: Int,
        p02: Int,
        p03: Int,
        p04: Int,
        p05: Int,
        p06: Int,
        p07: Int,
        p08: Int,
        p09: Int,
        p10: Int,
        p11: Int,
        p12: Int,
        p13: Int,
        p14: Int,
        p15: Int,
        p16: Int,
        p17: Int,
        p18: Int,
        p19: Int,
        p20: Int,
        p21: Int,
        p22: Int,
        p23: Int,
        p24: Int,
        p25: Int,
        p26: Int,
        p27: Int,
        p28: Int,
        p29: Int,
        p30: Int = 130
    ) {
        UiHookDefaultArgumentsProbe.originalArguments = listOf(p00, p30)
    }
}

@Composable
fun CallMemberBoundaryDefault() {
    MemberBoundaryContent().Content(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 24, 25, 26, 27, 28, 29
    )
}

@Composable
fun CallMemberBoundaryExplicit() {
    MemberBoundaryContent().Content(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 24, 25, 26, 27, 28, 29, 30
    )
}

@Composable
fun MultipleMasks(
    p00: Int = 100,
    p01: Int,
    p02: Int,
    p03: Int,
    p04: Int,
    p05: Int,
    p06: Int,
    p07: Int,
    p08: Int,
    p09: Int,
    p10: Int,
    p11: Int,
    p12: Int,
    p13: Int,
    p14: Int,
    p15: Int,
    p16: Int,
    p17: Int,
    p18: Int,
    p19: Int,
    p20: Int,
    p21: Int,
    p22: Int,
    p23: Int,
    p24: Int,
    p25: Int,
    p26: Int,
    p27: Int,
    p28: Int,
    p29: Int,
    p30: Int,
    p31: Int = 131
) {
    UiHookDefaultArgumentsProbe.originalArguments = listOf(p00, p31)
}

@Composable
fun CallFirstMaskDefault() {
    MultipleMasks(
        p01 = 1, p02 = 2, p03 = 3, p04 = 4, p05 = 5,
        p06 = 6, p07 = 7, p08 = 8, p09 = 9, p10 = 10,
        p11 = 11, p12 = 12, p13 = 13, p14 = 14, p15 = 15,
        p16 = 16, p17 = 17, p18 = 18, p19 = 19, p20 = 20,
        p21 = 21, p22 = 22, p23 = 23, p24 = 24, p25 = 25,
        p26 = 26, p27 = 27, p28 = 28, p29 = 29, p30 = 30,
        p31 = 31
    )
}

@Composable
fun CallSecondMaskDefault() {
    MultipleMasks(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 24, 25, 26, 27, 28, 29, 30
    )
}

@Composable
fun CallMultipleMasksExplicit() {
    MultipleMasks(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31
    )
}
