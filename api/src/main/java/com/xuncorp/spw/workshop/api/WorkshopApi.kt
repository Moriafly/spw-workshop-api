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
        @Deprecated(
            "使用 createConfigManager，作用一样",
            ReplaceWith("createConfigManager")
        )
        @UnstableSpwWorkshopApi
        @SinceApi("1.6.20", "0.1.0-dev09")
        fun createConfigManager(pluginId: String): ConfigManager

        @UnstableSpwWorkshopApi
        @SinceApi("1.7.0", "0.1.0-dev11")
        fun createConfigManager(): ConfigManager

        /**
         * 插件快捷键的注册与注销
         */
        @SinceApi("1.19.0", "0.1.0-dev21")
        val keyBindingManager: KeyBindingManager
    }

    /**
     * 播放相关
     */
    interface Playback {
        /**
         * 读取当前歌曲已解析的完整歌词时间轴
         *
         * 加载中、无歌词、加载失败或无时间戳文本均返回空列表
         *
         * 已持有的列表不会随宿主更新而改变，但可能已经不属于当前歌曲
         *
         * @return 完整歌词行列表；旧宿主实现使用此默认方法时返回空列表
         * @see PlaybackExtensionPoint.onLyricsLinesUpdated
         */
        @SinceApi("1.19.0", "0.1.0-dev21")
        fun getLyricsLines(): List<PlaybackExtensionPoint.LyricsLine> = emptyList()

        /**
         * 更改是否独占音频
         *
         * **必须在主线程调用**
         *
         * **禁止在 [PlaybackExtensionPoint.onIsPlayingChanged] 回调中调用此方法**
         */
        @SinceApi("1.3.16", "0.1.0-dev06")
        fun changeExclusive(exclusive: Boolean)

        /**
         * 暂停播放
         */
        @SinceApi("1.6.20", "0.1.0-dev10")
        fun pause()

        /**
         * 继续播放
         */
        @SinceApi("1.6.20", "0.1.0-dev10")
        fun play()

        /**
         * 上一首
         */
        @SinceApi("1.6.20", "0.1.0-dev10")
        fun previous()

        /**
         * 下一首
         */
        @SinceApi("1.6.20", "0.1.0-dev10")
        fun next()

        /**
         * 跳转到指定位置
         *
         * @param position 位置，单位毫秒
         */
        @SinceApi("1.7.0", "0.1.0-dev11")
        fun seekTo(position: Long)
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
