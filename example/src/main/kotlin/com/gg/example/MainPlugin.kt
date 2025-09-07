@file:OptIn(UnstableSpwWorkshopApi::class)

package com.gg.example

import com.xuncorp.spw.workshop.api.UnstableSpwWorkshopApi
import com.xuncorp.spw.workshop.api.WorkshopApi
import com.xuncorp.spw.workshop.api.config.ConfigHelper
import org.pf4j.Plugin

class MainPlugin : Plugin() {
    override fun start() {
        WorkshopApi.ui.toast("示例插件已启动", WorkshopApi.Ui.ToastType.Success)

        ConfigExample()
    }

    override fun stop() {
        WorkshopApi.ui.toast("示例插件已停止", WorkshopApi.Ui.ToastType.Warning)
    }

    override fun delete() {
        WorkshopApi.ui.toast("示例插件已删除", WorkshopApi.Ui.ToastType.Error)
    }

    companion object {
        @JvmStatic
        @JvmName("onExampleButtonClick")
        fun onExampleButtonClick() {
            val configHelper: ConfigHelper =
                WorkshopApi.manager
                    .createConfigManager("ExamplePlugin")
                    .getConfig("folder/config.json")

            configHelper.reload()
            configHelper.get("example.edittext", "试试在上方的输入框输入点什么？").let {
                WorkshopApi.ui.toast("配置项 example.edittext 的值是: $it", WorkshopApi.Ui.ToastType.Success)
            }
        }
    }
}
