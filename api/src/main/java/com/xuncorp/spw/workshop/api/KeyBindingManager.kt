package com.xuncorp.spw.workshop.api

/**
 * 插件快捷键管理
 *
 * 用于管理插件的快捷键注册和注销, 注册后快捷键可在 SPW 的快捷键管理中配置
 */
@SinceApi("1.19.0", "0.1.0-dev21")
interface KeyBindingManager {
    /**
     * 注册当前插件的快捷键
     * 用户绑定优先于 [defaultShortcut]；应用内键在文本输入时不触发，冲突键不触发
     * 句柄可从任意线程幂等关闭，插件停用时自动注销；已获执行资格的回调可完成
     *
     * @param actionId 快捷键 ID，最多 128 字符，只能包含字母、数字、点、下划线或连字符
     * @param title 非空显示名称，最多 128 字符
     * @param defaultShortcut 应用内默认组合键, 可为 null 表示不绑定默认组合键
     * @param hasGlobal 是否允许用户配置全局快捷键，关闭时保留已有键位但不触发
     * @param handler 快捷键触发时的回调
     *
     * @throws IllegalArgumentException 参数无效或 ID 重复
     * @throws IllegalStateException 无法识别插件或不在其 start 线程内注册
     */
    fun register(
        actionId: String,
        title: String,
        defaultShortcut: ActionShortcut?,
        hasGlobal: Boolean = false,
        handler: Runnable
    ): AutoCloseable

    /**
     * 注销当前插件的 [actionId]，无需添加插件 ID 前缀
     *
     * 可从任意线程重复调用，保留用户键位；已获执行资格的回调可完成
     *
     * @throws IllegalStateException 无法识别调用插件
     */
    fun unregister(actionId: String)
}
