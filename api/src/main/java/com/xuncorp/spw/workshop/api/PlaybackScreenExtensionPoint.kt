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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import org.pf4j.ExtensionPoint

/**
 * 播放界面扩展点
 */
interface PlaybackScreenExtensionPoint : ExtensionPoint {
    /**
     * 是否支持歌词面板
     */
    @SinceApi("1.9.2", "0.1.0-dev15")
    fun isSupportedLyricsPanel(): Boolean = false

    /**
     * 歌词面板
     *
     * 深浅色通过 `SaltTheme.configs.isDarkTheme` 判断，主题配色等类似
     *
     * @param onLineTime 歌词行时间戳（毫秒），每当跳转新行，将接收此行的开始时间戳
     * 当前 SPW 1.9 版本未公开 SPW 内部歌词数据，尽量依赖于 [onTime]
     * TODO SPW 公开其内部 LyricsDocument/LyricsLine/LyricsCell，避免不必要的当前行判断，或插件作者自行处理逻辑
     * @param onTime 当前播放时间戳（毫秒），在 SPW 1.9 中将以每秒 40 次的频率调用，此次数可视为 SPW 期望的歌词
     * 面板组件的帧率，此次数可能随版本升级而改变
     * @param modifier Compose Modifier
     * @param activated 歌词面板是否处于活跃状态
     * 当播放界面收起时，将处于非活跃状态，但此布局树依旧保留，请在非活跃状态时减少不必要的动画等资源消耗
     * @param showSubText 是否显示副文本（翻译等）
     * @param blurEffect 是否启用模糊效果模糊
     * @param threeEffect 是否启用 3D 效果
     * TODO karaokeCompatStrategy: KaraokeCompatStrategy = KaraokeCompatStrategy.Always
     * @param gradientPercentage 逐字渐变百分比
     * @param componentOffset 歌词面板组件偏移，100.dp 表示组件在 y 轴方向下移 100.dp（和 Compose 逻辑相反）
     * @param color 歌词主颜色
     * @param fontSize 歌词主文本字体大小
     * @param subTextFontSize 歌词副文本字体大小
     * @param fontStyle 歌词字体样式
     * @param fontWeight 歌词字体粗细
     * @param fontFamily 歌词字体
     * @param letterSpacing 歌词字间距
     * @param textDecoration 歌词文本修饰
     * @param textAlign 歌词文本对齐方式
     * @param lineHeight 歌词行高
     * @param style 歌词样式
     */
    @SinceApi("1.9.2", "0.1.0-dev15")
    @Composable
    fun LyricsPanel(
        onLineTime: () -> Long,
        onTime: () -> Long,
        modifier: Modifier = Modifier,
        activated: Boolean = true,
        showSubText: Boolean = true,
        blurEffect: Boolean = false,
        threeEffect: Boolean = false,
        gradientPercentage: Float = 0.25f,
        componentOffset: Dp = 0.dp,
        color: Color = Color.Unspecified,
        fontSize: TextUnit = SaltTheme.textStyles.main.fontSize,
        subTextFontSize: TextUnit = fontSize * 0.8f,
        fontStyle: FontStyle? = null,
        fontWeight: FontWeight? = null,
        fontFamily: FontFamily? = null,
        letterSpacing: TextUnit = TextUnit.Unspecified,
        textDecoration: TextDecoration? = null,
        textAlign: TextAlign? = null,
        lineHeight: TextUnit = TextUnit.Unspecified,
        style: TextStyle = SaltTheme.textStyles.main
    ) {
    }
}
