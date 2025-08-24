package com.xuncorp.spw.workshop.api.config

interface ConfigHelper {
    /**
     * 获取一个指定类型的配置项
     *
     * 如果配置文件中不存在该键，或值的类型不匹配，将返回指定的 [defaultValue].
     *
     * @param key 配置项的键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    fun <T> get(
        key: String,
        defaultValue: T
    ): T

    /**
     * 设置一个配置项的值
     *
     * 注意：这个方法仅在内存中更新配置，你需要调用 [save] 方法来将其持久化到磁盘。
     *
     * @param key 配置项的键
     * @param value 要设置的值
     */
    fun set(
        key: String,
        value: Any
    )

    /**
     * 保存当前所有配置项到插件的配置文件中
     *
     * @return true 如果保存成功, 否则 false
     */
    fun save(): Boolean

    /**
     * 从磁盘重新加载配置
     *
     * 这会覆盖所有在内存中尚未保存的修改。
     *
     * @return true 如果加载成功, 否则 false
     */
    fun reload(): Boolean
}