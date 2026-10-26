package com.xuncorp.spw.workshop.gradle;

import java.util.List;

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

    /**
     * 当前插件运行时依赖的其他插件，对应 Manifest 的 {@code Plugin-Dependencies}
     *
     * <p>使用 {@link PluginDependency#required(String)} 或 {@link PluginDependency#optional(String)}
     * 明确声明必需或可选依赖；版本条件作为 {@link PluginDependency#required(String, String)} 的第二个参数
     * 默认空列表，空列表或 {@code null} 不写入该属性；同一插件 ID 只能声明一次
     *
     * <p>Kotlin DSL 示例，需要导入 {@code com.xuncorp.spw.workshop.gradle.PluginDependency}
     * <pre>{@code
     * spmod {
     *     PluginDependencies = listOf(
     *         PluginDependency.required("com.example.library", ">=1.0.0 & <2.0.0"),
     *         PluginDependency.optional("com.example.integration")
     *     )
     * }
     * }</pre>
     */
    public List<PluginDependency> PluginDependencies = List.of();

    /**
     * 申请的宿主权限，对应 Plugin-Permissions，默认空列表
     * <p>
     * 声明后仍需用户在宿主中勾选授权
     */
    public List<PluginPermission> PluginPermissions = List.of();
}
