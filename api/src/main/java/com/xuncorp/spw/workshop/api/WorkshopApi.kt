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
     * 歌曲信息是不可修改的快照，封面字节数组的所有权见 [getCoverById]
     * 后续数据库或文件更新不会改变已持有的结果
     *
     * 音乐库查询均要求声明并获得 [PluginPermission.LIBRARY_READ]
     * 未声明、未授权时，stage 以 [PluginPermissionDeniedException] 失败
     * 停用保留授权，已接受的查询可在停用后完成；交付前撤销授权或卸载插件则拒绝交付
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
         * 按歌曲 ID 读取音频文件的内嵌封面
         *
         * 返回原始编码图片数据，如 JPEG 或 PNG
         * 每次成功调用返回独立的非空字节数组，调用方可以修改，不影响宿主缓存或其他调用结果
         *
         * @param id 音乐库歌曲 ID，与 [MediaItem.id] 一致，不是文件路径
         * @return ID 不存在、无内嵌封面或宿主无法读取封面时为 null；数据库查询失败通过 stage 报错
         */
        @SinceApi("1.19.0", "0.1.0-dev21")
        fun getCoverById(id: String): CompletionStage<ByteArray?>

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

        /**
         * 同步查询当前调用插件是否已获得 [permission]
         *
         * 未声明、未授权时返回 false
         * 停用保留授权，删除插件后清除；普通更新保留已确认的权限
         */
        @SinceApi("1.19.0", "0.1.0-dev21")
        fun isPermissionGranted(permission: PluginPermission): Boolean = false
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
         * 要求声明并获得 [PluginPermission.LIBRARY_READ]，无当前歌曲时也会校验权限
         * 查询期间撤销授权或卸载插件的拒绝通过 CompletionException 的 cause 返回
         *
         * @return 当前歌曲的 MediaItem 快照
         * @throws PluginPermissionDeniedException 调用时未声明、未授权或无法识别调用插件
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
