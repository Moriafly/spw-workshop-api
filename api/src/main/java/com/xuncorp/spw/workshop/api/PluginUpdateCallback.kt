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

import com.xuncorp.spw.workshop.api.UnstableSpwWorkshopApi
import com.xuncorp.spw.workshop.api.SinceApi

/**
 * 插件更新回调接口
 *
 * 插件可以实现此接口来处理更新前后的自定义逻辑
 */
interface PluginUpdateCallback {
    /**
     * 在插件更新检查开始时调用
     *
     * @param currentVersion 当前插件版本
     * @param updateUrl 更新检查的URL
     * @return 返回 false 可以取消此次更新检查
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun onUpdateCheckStarted(currentVersion: String, updateUrl: String): Boolean = true

    /**
     * 在发现可用更新时调用
     *
     * @param currentVersion 当前插件版本
     * @param newVersion 新版本号
     * @param changelog 更新日志（可能为null）
     * @param downloadUrl 更新包下载URL
     * @return 返回 false 可以拒绝此次更新
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun onUpdateAvailable(
        currentVersion: String,
        newVersion: String,
        changelog: String?,
        downloadUrl: String
    ): Boolean = true

    /**
     * 在更新下载开始时调用
     *
     * @param downloadUrl 下载URL
     * @param fileSize 文件大小（字节），如果未知则为-1
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun onUpdateDownloadStarted(downloadUrl: String, fileSize: Long) {}

    /**
     * 更新下载进度回调
     *
     * @param bytesDownloaded 已下载字节数
     * @param totalBytes 总字节数，如果未知则为-1
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun onUpdateDownloadProgress(bytesDownloaded: Long, totalBytes: Long) {}

    /**
     * 在更新下载完成时调用
     *
     * @param filePath 下载文件的本地路径
     * @param fileSize 文件大小（字节）
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun onUpdateDownloadCompleted(filePath: String, fileSize: Long) {}

    /**
     * 在更新验证开始时调用
     *
     * @param filePath 更新包文件路径
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun onUpdateVerificationStarted(filePath: String) {}

    /**
     * 在更新验证完成时调用
     *
     * @param filePath 更新包文件路径
     * @param isValid 验证是否通过
     * @param message 验证结果消息
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun onUpdateVerificationCompleted(filePath: String, isValid: Boolean, message: String?) {}

    /**
     * 在更新安装前调用
     *
     * 插件可以在此方法中备份当前状态或数据
     *
     * @param currentVersion 当前版本
     * @param newVersion 新版本
     * @return 返回 false 可以取消更新安装
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun onBeforeUpdateInstall(currentVersion: String, newVersion: String): Boolean = true

    /**
     * 在更新安装完成后调用
     *
     * 插件可以在此方法中恢复状态或迁移数据
     *
     * @param oldVersion 旧版本
     * @param newVersion 新版本
     * @param success 安装是否成功
     * @param errorMessage 错误信息（如果安装失败）
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun onAfterUpdateInstall(oldVersion: String, newVersion: String, success: Boolean, errorMessage: String?) {}

    /**
     * 在更新过程完成时调用（无论成功或失败）
     *
     * @param success 整个更新过程是否成功
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun onUpdateCompleted(success: Boolean) {}
}

/**
 * 插件更新管理器接口
 *
 * 宿主程序应该实现此接口来处理插件更新
 */
interface PluginUpdateManager {
    /**
     * 检查所有插件的更新
     *
     * @param force 是否强制检查（忽略缓存）
     * @param callback 进度回调
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun checkForUpdates(force: Boolean = false, callback: UpdateCheckCallback? = null)

    /**
     * 安装可用更新
     *
     * @param pluginId 要更新的插件ID，如果为null则更新所有有可用更新的插件
     * @param callback 进度回调
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun installUpdates(pluginId: String? = null, callback: UpdateInstallCallback? = null)

    /**
     * 获取插件的更新信息
     *
     * @param pluginId 插件ID
     * @return 更新信息，如果没有可用更新则返回null
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun getUpdateInfo(pluginId: String): PluginUpdateInfo?

    /**
     * 设置自动更新检查间隔
     *
     * @param intervalMillis 间隔时间（毫秒），设置为0禁用自动更新检查
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun setAutoUpdateCheckInterval(intervalMillis: Long)
    
    /**
     * 获取自动更新检查间隔
     *
     * @return 自动更新检查间隔（毫秒）
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun getAutoUpdateCheckInterval(): Long
    
    /**
     * 立即执行自动更新检查（如果启用）
     */
    @SinceApi("1.7.0", "0.1.0-dev14")
    fun performAutoUpdateCheck()
}

/**
 * 插件更新信息
 */
data class PluginUpdateInfo(
    val pluginId: String,
    val currentVersion: String,
    val newVersion: String,
    val changelog: String?,
    val downloadUrl: String,
    val releaseDate: Long,
    val fileSize: Long
)

/**
 * 更新检查回调接口
 */
interface UpdateCheckCallback {
    fun onCheckStarted(pluginCount: Int) {}
    fun onPluginCheckStarted(pluginId: String) {}
    fun onPluginCheckCompleted(pluginId: String, updateAvailable: Boolean) {}
    fun onCheckCompleted(updatesAvailable: Int) {}
    fun onError(pluginId: String?, error: Throwable) {}
}

/**
 * 更新安装回调接口
 */
interface UpdateInstallCallback {
    fun onInstallStarted(pluginCount: Int) {}
    fun onPluginInstallStarted(pluginId: String) {}
    fun onPluginInstallProgress(pluginId: String, progress: Float) {}
    fun onPluginInstallCompleted(pluginId: String, success: Boolean) {}
    fun onInstallCompleted(successfulCount: Int, failedCount: Int) {}
    fun onError(pluginId: String?, error: Throwable) {}
}