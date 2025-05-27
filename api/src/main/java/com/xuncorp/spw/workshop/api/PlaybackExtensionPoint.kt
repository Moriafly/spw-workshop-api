@file:Suppress("unused")

package com.xuncorp.spw.workshop.api

import org.pf4j.ExtensionPoint

/**
 * 播放拓展点
 */
interface PlaybackExtensionPoint : ExtensionPoint {
    /**
     * 更改是否独占音频
     *
     * **必须在主线程调用**
     */
    fun changeExclusive(exclusive: Boolean)
}
