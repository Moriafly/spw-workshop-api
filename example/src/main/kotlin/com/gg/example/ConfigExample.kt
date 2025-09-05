@file:OptIn(UnstableSpwWorkshopApi::class)

package com.gg.example

import com.xuncorp.spw.workshop.api.UnstableSpwWorkshopApi
import com.xuncorp.spw.workshop.api.WorkshopApi
import com.xuncorp.spw.workshop.api.config.ConfigHelper
import com.xuncorp.spw.workshop.api.config.ConfigManager

class ConfigExample {
    // 创建配置管理器实例，参数为插件的唯一标识符 必须与插件描述文件中的 ID 一致
    val configManager: ConfigManager = WorkshopApi.manager.createConfigManager("ExamplePlugin")
    val configHelper: ConfigHelper = configManager.getConfig() // 获取默认的配置文件帮助程序 (preference_config.json)

    /**
     * 演示如何使用配置管理器和配置帮助程序
     */
    fun demonstrateConfigUsage() {
        // 设置配置项
        configHelper.set("exampleKey", "exampleValue")
        configHelper.set("nested.setting", 42) // 使用点号表示嵌套的 JSON 结构
        configHelper.set("featureEnabled", true)
        configHelper.save() // 保存配置到磁盘

        // 获取配置项
        configHelper.reload() // 重新加载配置以确保获取的是最新值
        val value = configHelper.get("exampleKey", "defaultValue")
        WorkshopApi.ui.toast("配置项 exampleKey 的值是: $value", WorkshopApi.Ui.ToastType.Success)
    }

    /**
     * 监听配置文件变化
     * 当配置文件被外部修改并保存时，会触发回调
     *
     * 当用户在 SPW 中修改配置项并保存时，也会触发回调
     */
    fun listenToConfigChanges() {
        configManager.addConfigChangeListener { configHelper ->
            configHelper.reload() // 重新加载配置以获取最新值

            val updatedValue = configHelper.get("list_key", "defaultValue")
            WorkshopApi.ui.toast("配置项 exampleKey 已更新为: $updatedValue", WorkshopApi.Ui.ToastType.Success)
        }
    }

    init {
        listenToConfigChanges()
    }
}
