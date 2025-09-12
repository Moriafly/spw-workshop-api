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

package com.xuncorp.spw.workshop.api

import org.pf4j.Plugin

/**
 * SPW 插件基类
 *
 * 所有 SPW 插件都必须继承自此类
 *
 * @param pluginContext 插件上下文，提供插件运行时的信息
 */
abstract class SpwPlugin(
    val pluginContext: PluginContext
) : Plugin(), PluginUpdateCallback {
    
    // 默认实现 PluginUpdateCallback 的所有方法
    // 插件可以重写这些方法来实现自定义的更新逻辑
    
    override fun onUpdateCheckStarted(currentVersion: String, updateUrl: String): Boolean {
        // 默认允许更新检查
        return true
    }
    
    override fun onUpdateAvailable(
        currentVersion: String,
        newVersion: String,
        changelog: String?,
        downloadUrl: String
    ): Boolean {
        // 默认接受更新
        return true
    }
    
    override fun onUpdateDownloadStarted(downloadUrl: String, fileSize: Long) {

    }
    
    override fun onUpdateDownloadProgress(bytesDownloaded: Long, totalBytes: Long) {

    }
    
    override fun onUpdateDownloadCompleted(filePath: String, fileSize: Long) {

    }
    
    override fun onUpdateVerificationStarted(filePath: String) {

    }
    
    override fun onUpdateVerificationCompleted(filePath: String, isValid: Boolean, message: String?) {

    }
    
    override fun onBeforeUpdateInstall(currentVersion: String, newVersion: String): Boolean {
        // 默认允许更新安装
        return true
    }
    
    override fun onAfterUpdateInstall(oldVersion: String, newVersion: String, success: Boolean, errorMessage: String?) {

    }
    
    override fun onUpdateCompleted(success: Boolean) {

    }
}