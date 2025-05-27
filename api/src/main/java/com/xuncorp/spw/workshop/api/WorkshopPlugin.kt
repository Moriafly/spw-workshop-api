@file:Suppress("unused")

package com.xuncorp.spw.workshop.api

import org.pf4j.Plugin

abstract class WorkshopPlugin(
    private val workshopApi: WorkshopApi
) : Plugin()
