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

/**
 * SPW 创意工坊 API
 */
interface WorkshopApi {
    val playback: Playback

    interface Playback {
        /**
         * 更改是否独占音频
         *
         * **必须在主线程调用**
         *
         * **禁止在 [PlaybackExtensionPoint.onIsPlayingChanged] 回调中调用此方法**
         */
        fun changeExclusive(exclusive: Boolean)
    }

    companion object {
        /**
         * SPW 程序自行启动注入
         *
         * **仅调用，请勿赋值**
         */
        lateinit var instance: WorkshopApi
    }
}
