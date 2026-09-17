package com.xuncorp.spw.workshop.api

/**
 * 插件动作的应用内组合键
 *
 * @property keyCode [java.awt.event.KeyEvent] 的 VK_* 键码，必须为正值
 * @property modifiers 本类型修饰键常量的位掩码，0 表示不使用修饰键
 * @throws IllegalArgumentException 键码或修饰键不满足约束
 */
@SinceApi("1.19.0", "0.1.0-dev21")
class ActionShortcut(
    val keyCode: Int,
    val modifiers: Int
) {
    init {
        require(keyCode > 0) {
            "keyCode must be a positive AWT KeyEvent.VK_* code"
        }
        require(modifiers and (CONTROL or ALT or SHIFT or META).inv() == 0) {
            "Unknown shortcut modifiers"
        }
    }

    override fun equals(other: Any?): Boolean =
        other is ActionShortcut && keyCode == other.keyCode && modifiers == other.modifiers

    override fun hashCode(): Int = 31 * keyCode + modifiers

    override fun toString(): String = "ActionShortcut(keyCode=$keyCode, modifiers=$modifiers)"

    companion object {
        const val CONTROL: Int = 1
        const val ALT: Int = 2
        const val SHIFT: Int = 4
        const val META: Int = 8
    }
}
