package com.gg.example

import com.xuncorp.spw.workshop.api.PlaybackExtensionPoint.MediaItem
import com.xuncorp.spw.workshop.api.WorkshopApi
import java.util.concurrent.CompletionStage

object LibraryExample {
    fun printFirstPage(): CompletionStage<Void> =
        WorkshopApi.library.getTracks(afterId = null, limit = 20)
            .thenAccept { tracks ->
                tracks.forEach { track: MediaItem ->
                    println("${track.id}: ${track.title} / ${track.artist} (${track.duration} ms)")
                }
            }
            .exceptionally { error ->
                error.printStackTrace()
                null
            }
}
