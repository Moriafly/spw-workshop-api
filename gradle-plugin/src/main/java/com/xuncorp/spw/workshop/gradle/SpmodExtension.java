package com.xuncorp.spw.workshop.gradle;

/**
 * spmod 配置块，字段名与插件 Manifest 中的元数据对应
 *
 * <p>PluginClass、PluginId 和 PluginVersion 必填，其余字段可选
 * 配置值在 Gradle 读取任务输入时使用，可直接通过等号赋值
 */
public class SpmodExtension {
    /**
     * 插件主类的完整类名，对应 Plugin-Class
     */
    public String PluginClass;

    /**
     * 唯一插件 ID，对应 Plugin-Id，同时用于分发包文件名
     */
    public String PluginId;

    /**
     * 插件版本，对应 Plugin-Version，同时用于分发包文件名
     */
    public String PluginVersion;

    /**
     * 插件作者，对应 Plugin-Provider，未设置时不写入 Manifest
     */
    public String PluginProvider;

    /**
     * 插件显示名称，对应 Plugin-Name，未设置时不写入 Manifest
     */
    public String PluginName;

    /**
     * 插件描述，对应 Plugin-Description，未设置时不写入 Manifest
     */
    public String PluginDescription;

    /**
     * 是否提供配置文件，对应 Plugin-Has-Config，默认 false
     */
    public boolean PluginHasConfig;

    /**
     * 插件开源地址，对应 Plugin-Open-Source-Url，未设置时不写入 Manifest
     */
    public String PluginOpenSourceUrl;
}
