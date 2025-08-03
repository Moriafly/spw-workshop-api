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

import org.pf4j.ExtensionPoint

/**
 * 播放拓展点
 */
interface PlaybackExtensionPoint : ExtensionPoint {
    /**
     * 播放器状态改变的回调
     */
    @SinceAPI("1.3.16", "0.1.0-dev06")
    fun onStateChanged(state: State)

    /**
     * 正在播放状态改变的回调
     */
    @SinceAPI("1.3.16", "0.1.0-dev06")
    fun onIsPlayingChanged(isPlaying: Boolean)

    /**
     * 跳转到 [position] ms 的回调
     */
    @SinceAPI("1.3.16", "0.1.0-dev06")
    fun onSeekTo(position: Long)

    /**
     * 加载歌词（优先）的回调，返回 null 表示加载歌词将使用 SPW 默认逻辑
     */
    @SinceAPI("1.3.16", "0.1.0-dev06")
    fun updateLyrics(mediaItem: MediaItem): String?

    enum class State {
        Idle,
        Buffering,
        Ready,
        Ended
    }

    /**
     * 媒体项
     *
     * @property title 标题
     * @property artist 艺术家，多艺术家以“/”分割（含特殊情况）
     * @property album 专辑，多专辑以“/”分割（含特殊情况）
     * @property albumArtist 专辑艺术家，多专辑艺术家以“/”分割（含特殊情况）
     * @property path 文件路径，平台风格，如：C:\Music\Song.mp3
     */
    data class MediaItem(
        val title: String,
        val artist: String,
        val album: String,
        val albumArtist: String,
        val path: String
    )
}
