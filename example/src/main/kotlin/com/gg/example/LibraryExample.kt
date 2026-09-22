package com.gg.example

import com.xuncorp.spw.workshop.api.PlaybackExtensionPoint.MediaItem
import com.xuncorp.spw.workshop.api.PluginPermissionDeniedException
import com.xuncorp.spw.workshop.api.WorkshopApi
import java.util.concurrent.CompletionStage
import java.util.concurrent.CompletionException

object LibraryExample {
    fun printFirstPage(): CompletionStage<Void> =
        WorkshopApi.library.getTracks(afterId = null, limit = 20)
            .thenAccept { tracks ->
                tracks.forEach { track: MediaItem ->
                    println("${track.id}: ${track.title} / ${track.artist} (${track.duration} ms)")
                }
            }
            .exceptionally { error ->
                val cause = (error as? CompletionException)?.cause ?: error
                if (cause is PluginPermissionDeniedException) {
                    println("曲库读取权限未授予")
                } else {
                    cause.printStackTrace()
                }
                null
            }
}
