package com.xuncorp.spw.workshop.gradle;

/**
 * 在 spmod.PluginPermissions 中声明的宿主权限
 *
 * <p>打包时转换为稳定的 Manifest 标识，声明后仍需用户在宿主中勾选授权
 */
public enum PluginPermission {
    /**
     * 允许插件注册快捷键，并由用户配置应用内或全局组合键
     */
    KEY_BINDINGS("key-bindings"),

    /**
     * 允许插件读取曲库歌曲元数据、文件路径、收藏状态和内嵌封面，包括当前歌曲的元数据查询
     */
    LIBRARY_READ("library-read"),

    /**
     * 允许插件写入曲库数据，不包含读取权限
     */
    LIBRARY_WRITE("library-write");

    private final String id;

    PluginPermission(String id) {
        this.id = id;
    }

    /**
     * Plugin-Permissions 清单属性使用的稳定标识
     */
    public String getId() {
        return id;
    }
}
