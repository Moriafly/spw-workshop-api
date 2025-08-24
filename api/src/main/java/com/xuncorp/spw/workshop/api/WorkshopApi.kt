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

package com.xuncorp.spw.workshop.api

import com.xuncorp.spw.workshop.api.config.ConfigManager

/**
 * SPW 创意工坊 API
 */
interface WorkshopApi {
    val playback: Playback

    val ui: Ui

    val manager: Manager

    /**
     * 实用工具相关
     */
    interface Manager {
        /**
         * 创建一个配置管理器
         *
         * @param pluginId 插件 ID
         */
        @SinceApi("1.6.20", "0.1.0-dev09")
        fun createConfigManager(pluginId: String): ConfigManager
    }

    /**
     * 播放相关
     */
    interface Playback {
        /**
         * 更改是否独占音频
         *
         * **必须在主线程调用**
         *
         * **禁止在 [PlaybackExtensionPoint.onIsPlayingChanged] 回调中调用此方法**
         */
        @SinceApi("1.3.16", "0.1.0-dev06")
        fun changeExclusive(exclusive: Boolean)
    }

    /**
     * 界面相关
     */
    interface Ui {
        /**
         * 发送一个 [type] 类型文本吐司
         */
        @SinceApi("1.5.20", "0.1.0-dev07")
        fun toast(text: String, type: ToastType)

        enum class ToastType {
            Success,
            Warning,
            Error
        }
    }

    companion object {
        /**
         * SPW 程序自行启动注入
         *
         * **Mod 制作方仅调用（get），请勿赋值（set）**
         */
        lateinit var instance: WorkshopApi

        val ui: Ui
            @JvmStatic
            @JvmName("ui")
            get() = instance.ui

        val playback: Playback
            @JvmStatic
            @JvmName("playback")
            get() = instance.playback

        val manager: Manager
            @JvmStatic
            @JvmName("manager")
            get() = instance.manager
    }
}
