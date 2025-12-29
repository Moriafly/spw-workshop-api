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

package com.xuncorp.spw.workshop.api.data.lyrics

import androidx.compose.runtime.Immutable
import com.xuncorp.spw.workshop.api.UnstableSpwWorkshopApi

/**
 * 歌词文档
 *
 * @param sourceText 源文本（经过 SPW 格式化后的）
 * @param lyricsLines 歌词行列表，以按照行开始时间顺序排列
 * @param isSupportKaraoke 是否支持卡拉 OK 逐字
 * @param isScrollable 是否支持滚动歌词
 * @param isEmpty 是否为空歌词文档
 *
 * @see LyricsLine
 * @see LyricsCell
 */
@UnstableSpwWorkshopApi
@Immutable
data class LyricsDocument(
    val sourceText: String,
    val lyricsLines: List<LyricsLine>,
    val isSupportKaraoke: Boolean,
    val isScrollable: Boolean,
    val isEmpty: Boolean
)
