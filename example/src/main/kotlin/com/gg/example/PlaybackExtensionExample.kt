package com.gg.example

import com.xuncorp.spw.workshop.api.PlaybackExtensionPoint
import com.xuncorp.spw.workshop.api.WorkshopApi
import org.pf4j.Extension

/**
 * 播放扩展示例
 *
 * 这个扩展展示了如何监听播放器的各种状态变化：
 * - 播放状态变化（空闲、缓冲、就绪、结束）
 * - 播放/暂停状态变化
 * - 进度跳转
 * - 歌词加载和更新
 * - 播放位置更新
 */
@Extension
class PlaybackExtensionExample : PlaybackExtensionPoint {
    private var currentMediaItem: PlaybackExtensionPoint.MediaItem? = null
    private var currentPosition = 0L

    /**
     * 播放器状态改变回调
     */
    override fun onStateChanged(state: PlaybackExtensionPoint.State) {
        when (state) {
            PlaybackExtensionPoint.State.Idle -> {
                WorkshopApi.ui.toast("播放器空闲", WorkshopApi.Ui.ToastType.Warning)
            }
            PlaybackExtensionPoint.State.Buffering -> {
                WorkshopApi.ui.toast("正在缓冲音频", WorkshopApi.Ui.ToastType.Warning)
            }
            PlaybackExtensionPoint.State.Ready -> {
                WorkshopApi.ui.toast("播放器就绪", WorkshopApi.Ui.ToastType.Success)
            }
            PlaybackExtensionPoint.State.Ended -> {
                WorkshopApi.ui.toast("播放结束", WorkshopApi.Ui.ToastType.Success)
            }
        }
    }

    /**
     * 播放/暂停状态变化回调
     */
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        WorkshopApi.ui.toast("▶️ 播放状态: ${if (isPlaying) "播放中" else "已暂停"}", WorkshopApi.Ui.ToastType.Success)
    }

    /**
     * 进度跳转回调
     */
    override fun onSeekTo(position: Long) {
        WorkshopApi.ui.toast("⏭️ 跳转到位置: ${formatTime(position)}", WorkshopApi.Ui.ToastType.Success)
    }

    /**
     * 加载歌词前的回调
     * 返回 null 表示使用默认逻辑
     */
    override fun onBeforeLoadLyrics(mediaItem: PlaybackExtensionPoint.MediaItem): String? {
        // 可以利用此回调记录当前播放的媒体项
        this.currentMediaItem = mediaItem

        // 这里可以实现自定义歌词加载逻辑
        // 例如从特定的歌词服务器获取歌词
        // 返回 null 表示使用 SPW 默认的歌词加载逻辑
        return null
    }

    /**
     * 加载歌词后的回调（当默认逻辑无法加载歌词时调用）
     */
    override fun onAfterLoadLyrics(mediaItem: PlaybackExtensionPoint.MediaItem): String? {
        // 这里可以实现备用歌词加载逻辑
        // 例如从本地文件或其他歌词服务获取

        // 示例：返回一个简单的歌词
        return if (mediaItem.title.contains("示例", ignoreCase = true)) {
            """[00:00.00]这是一首示例歌词
[00:05.00]由示例插件提供歌词
[00:10.00]展示歌词加载功能
[00:15.00]感谢使用 SPW 创意工坊"""
        } else {
            null // 返回 null 表示也无法提供歌词
        }
    }

    /**
     * 当前播放歌词行更新回调
     */
    override fun onLyricsLineUpdated(lyricsLine: PlaybackExtensionPoint.LyricsLine?) {
        if (lyricsLine != null) {
            WorkshopApi.ui.toast("🎤 当前歌词: ${lyricsLine.pureMainText}", WorkshopApi.Ui.ToastType.Success)
        } else {
            WorkshopApi.ui.toast("🎤 当前无歌词", WorkshopApi.Ui.ToastType.Warning)
        }
    }

    /**
     * 播放位置更新回调（每秒调用一次）
     */
    override fun onPositionUpdated(position: Long) {
        this.currentPosition = position
        println("⏱️ 播放位置: ${formatTime(position)}")
    }

    /**
     * 格式化时间显示
     */
    private fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}
