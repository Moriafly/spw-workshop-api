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
 * 歌词行
 *
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param lyricsCells 歌词单元
 * @param pureMainText 纯文本（主要歌词文本）
 * @param pureSubText 纯翻译文本
 */
@UnstableSpwWorkshopApi
@Immutable
data class LyricsLine(
    val startTime: Long,
    val endTime: Long,
    val lyricsCells: List<LyricsCell>,
    val pureMainText: String,
    val pureSubText: String?
)
