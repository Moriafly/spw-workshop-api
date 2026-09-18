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

import com.xuncorp.spw.workshop.api.PlaybackExtensionPoint.MediaItem
import com.xuncorp.spw.workshop.api.config.ConfigManager
import java.util.concurrent.CompletionStage

/**
 * SPW 创意工坊 API
 */
interface WorkshopApi {
    val playback: Playback

    val ui: Ui

    val manager: Manager

    @SinceApi("1.19.0", "0.1.0-dev21")
    val library: Library

    /**
     * 查询音乐库中的歌曲信息
     *
     * 返回值是不可修改的快照，后续数据库更新不会改变已持有的结果
     *
     * API 注入后即可查询，已接受的查询可在插件停用后完成
     * 插件负责停用后后续动作的处理；取消转换出的 future 不承诺取消数据库读取
     * 非 Async 后续动作可能在完成线程或注册动作的线程执行，不保证固定线程
     */
    @SinceApi("1.19.0", "0.1.0-dev21")
    interface Library {
        /**
         * 按歌曲 ID 查询
         *
         * @return 查询时对应的歌曲，ID 不存在时为 null
         */
        fun getTrackById(id: String): CompletionStage<MediaItem?>

        /**
         * 一次查询完整曲库，按 ID 的数据库 BINARY 顺序升序排列
         *
         * 空库返回空列表
         */
        fun getAllTracks(): CompletionStage<List<MediaItem>>

        /**
         * 按 ID 游标分页查询，按数据库 BINARY 顺序升序排列
         *
         * 每页独立读取最新状态，不承诺跨页快照一致性
         * 已经过的 ID 区间内新增的歌曲可能不会出现在本轮遍历中
         * 需要单次一致结果时使用 [getAllTracks]
         *
         * @param afterId null 表示首页，否则只返回 ID 大于此值的歌曲；该 ID 不必仍存在
         * @param limit 正数，不设额外上限；非法值通过失败的 stage 返回 IllegalArgumentException
         * @return 至多 limit 首歌曲；不足 limit 表示此次查询已到末尾，下一页传本页最后一首的 ID
         */
        fun getTracks(afterId: String?, limit: Int): CompletionStage<List<MediaItem>>
    }

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
         * 获取当前歌曲的 MediaItem 快照
         *
         * 无当前歌曲时返回 null
         * 数据与 [Library.getTrackById] 一致，返回不可变快照，后续更新不会改变旧结果
         * 调用会阻塞直到数据库查询完成，建议在后台线程调用
         *
         * @return 当前歌曲的 MediaItem 快照
         */
        @SinceApi("1.19.0", "0.1.0-dev21")
        fun getCurrentMediaItem(): MediaItem?

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

        @SinceApi("1.19.0", "0.1.0-dev21")
        val library: Library
            @JvmStatic
            @JvmName("library")
            get() = instance.library
    }
}
