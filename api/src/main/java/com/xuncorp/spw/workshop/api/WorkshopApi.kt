@file:Suppress("unused")

package com.xuncorp.spw.workshop.api

interface WorkshopApi {
    val playback: Playback

    interface Playback {
        fun isPlaying(): Boolean
    }
}
