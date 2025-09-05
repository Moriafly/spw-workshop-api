package com.xuncorp.spw.workshop.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.pf4j.ExtensionPoint

interface PlayerLyricsExtensionPoints : ExtensionPoint {
    @SinceApi("1.7", "0.1.0-dev11")
    @Composable
    fun buildUi(modifier: Modifier)
}
