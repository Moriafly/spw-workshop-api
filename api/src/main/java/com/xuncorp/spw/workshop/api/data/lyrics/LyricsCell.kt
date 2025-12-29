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
 * 卡拉 OK 歌词中最小的可滚动单元
 *
 * @property startTime 开始时间戳
 * @property endTime 结束时间戳
 * @property text 文本
 */
@UnstableSpwWorkshopApi
@Immutable
data class LyricsCell(
    val startTime: Long,
    val endTime: Long,
    val text: String
)
