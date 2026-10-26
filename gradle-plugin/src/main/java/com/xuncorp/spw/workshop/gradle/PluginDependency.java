package com.xuncorp.spw.workshop.gradle;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.expr.ExpressionParser;

/**
 * 在 {@link SpmodExtension#PluginDependencies} 中声明对另一个插件的运行时依赖
 *
 * <p>使用 {@link #required(String)} 声明必需依赖，使用 {@link #optional(String)} 声明可选依赖
 * 需要限制必需依赖的版本时，使用 {@link #required(String, String)} 单独传入版本条件
 * 插件 ID 使用对方的 {@code PluginId}，不使用显示名称、Maven 坐标或创意工坊条目 ID
 *
 * <p>声明不可变，打包时自动转换为 PF4J 元数据；不会下载、订阅或打包被依赖的插件
 */
public final class PluginDependency {
    private final String pluginId;
    private final String versionRequirement;
    private final boolean optional;

    private PluginDependency(
            String pluginId,
            String versionRequirement,
            boolean optional
    ) {
        if (pluginId == null
                || !pluginId.matches("[A-Za-z0-9_][A-Za-z0-9_-]*(?:\\.[A-Za-z0-9_][A-Za-z0-9_-]*)*")) {
            throw new IllegalArgumentException(
                    "pluginId 必须是有效的插件 ID，例如 com.example.library；不要附加版本或可选标记"
            );
        }
        if (versionRequirement == null || versionRequirement.isBlank()
                || versionRequirement.indexOf(',') >= 0 || versionRequirement.indexOf('@') >= 0
                || versionRequirement.indexOf('?') >= 0 || versionRequirement.indexOf('\n') >= 0
                || versionRequirement.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(
                    "插件 " + pluginId + " 的 versionRequirement 无效：" + versionRequirement
                            + "；必须是非空版本条件，且不能包含逗号、换行、@ 或 ?"
            );
        }
        String requirement = versionRequirement.trim();
        // PF4J 将 * 直接视为任意版本，其余条件使用同一解析器校验语法
        if (!"*".equals(requirement)) {
            try {
                ExpressionParser.newInstance().parse(requirement);
            } catch (ParseException | IllegalArgumentException failure) {
                throw new IllegalArgumentException(
                        "插件 " + pluginId + " 的 versionRequirement 语法无效：" + requirement,
                        failure
                );
            }
        }
        this.pluginId = pluginId;
        this.versionRequirement = requirement;
        this.optional = optional;
    }

    /**
     * 声明必需依赖，接受任意版本；未安装该插件时，当前插件无法加载
     *
     * @param pluginId 被依赖插件的 ID，例如 {@code com.example.library}
     * @return 不限制版本的必需依赖
     * @throws IllegalArgumentException ID 为 {@code null}、为空或不符合插件 ID 格式
     */
    public static PluginDependency required(String pluginId) {
        return required(pluginId, "*");
    }

    /**
     * 声明带版本条件的必需依赖；未安装或版本不符合时，当前插件无法加载
     *
     * <p>版本条件示例：{@code 1.2.3} 表示指定版本，{@code >=1.0.0} 表示最低版本，
     * {@code >=1.0.0 & <2.0.0} 表示至少为 {@code 1.0.0} 且低于 {@code 2.0.0}
     * 使用 PF4J 语义化版本表达式，不使用 Maven 的 {@code [1.0,2.0)} 区间语法
     * 创建声明时使用与宿主 PF4J 相同的解析器校验语法，非法表达式会使 Gradle 配置失败
     * 已安装插件的版本是否满足条件，仍由宿主中的 PF4J 校验
     *
     * @param pluginId 被依赖插件的 ID，不附加版本或可选标记
     * @param versionRequirement 非空版本条件，首尾空白会移除，{@code *} 表示接受任意版本
     * @return 带版本条件的必需依赖
     * @throws IllegalArgumentException ID 不符合插件 ID 格式，或版本条件为 {@code null}、为空、
     *         含有逗号、换行、{@code @}、{@code ?} 或无法解析为版本表达式
     */
    public static PluginDependency required(String pluginId, String versionRequirement) {
        return new PluginDependency(
                pluginId,
                versionRequirement,
                false
        );
    }

    /**
     * 声明可选依赖，未安装该插件时仍允许加载当前插件
     *
     * <p>PF4J 不校验可选依赖的版本，因此此入口不接受版本条件
     * 插件需在使用可选能力前自行确认是否可用
     *
     * @param pluginId 被依赖插件的 ID，不附加可选标记
     * @return 不限制版本的可选依赖
     * @throws IllegalArgumentException ID 为 {@code null}、为空或不符合插件 ID 格式
     */
    public static PluginDependency optional(String pluginId) {
        return new PluginDependency(
                pluginId,
                "*",
                true
        );
    }

    /**
     * 获取被依赖插件的 ID
     *
     * @return 非空插件 ID
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * 获取必需依赖的版本条件
     *
     * @return 去除首尾空白的版本条件，不限制版本时为 {@code *}，可选依赖始终为 {@code *}
     */
    public String getVersionRequirement() {
        return versionRequirement;
    }

    /**
     * 获取该依赖是否允许未安装
     *
     * @return 可选依赖为 {@code true}，必需依赖为 {@code false}
     */
    public boolean isOptional() {
        return optional;
    }
}
