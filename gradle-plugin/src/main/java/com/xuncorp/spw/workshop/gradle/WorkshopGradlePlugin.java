package com.xuncorp.spw.workshop.gradle;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.bundling.Jar;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 提供 spmod 配置块，生成插件 Manifest 并自动注册 plugin 分发任务
 */
public class WorkshopGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        SpmodExtension spmod = project.getExtensions().create("spmod", SpmodExtension.class);
        ProviderFactory providers = project.getProviders();
        Provider<String> pluginClass = providers.provider(() -> required("PluginClass", spmod.PluginClass));
        Provider<String> pluginId = providers.provider(() -> required("PluginId", spmod.PluginId));
        Provider<String> pluginVersion = providers.provider(() -> required("PluginVersion", spmod.PluginVersion));

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("Plugin-Class", pluginClass);
        attributes.put("Plugin-Id", pluginId);
        attributes.put("Plugin-Version", pluginVersion);
        attributes.put("Plugin-Provider", providers.provider(() -> spmod.PluginProvider));
        attributes.put("Plugin-Name", providers.provider(() -> spmod.PluginName));
        attributes.put("Plugin-Description", providers.provider(() -> spmod.PluginDescription));
        attributes.put("Plugin-Has-Config", providers.provider(() -> spmod.PluginHasConfig));
        attributes.put("Plugin-Open-Source-Url", providers.provider(() -> spmod.PluginOpenSourceUrl));
        project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class).configure(jar -> {
            jar.getManifest().attributes(attributes);
            // 中间 JAR 单独存放，build/libs 只输出插件分发包
            jar.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("intermediates/spmod"));
        });

        project.getTasks().register(
                "plugin",
                WorkshopPluginTask.class,
                task -> {
                    task.getPluginId().set(pluginId);
                    task.getPluginVersion().set(pluginVersion);
                }
        );
    }

    private static String required(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new GradleException("spmod." + name + " 不能为空");
        }
        return value;
    }
}
