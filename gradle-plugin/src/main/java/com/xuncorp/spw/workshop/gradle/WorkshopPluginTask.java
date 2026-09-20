package com.xuncorp.spw.workshop.gradle;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;

/**
 * 为 Salt Player 使用的 .spmod 分发包
 *
 * <p>通过 spmod 配置插件元数据，jar 的内容展开到 classes/，
 * runtimeClasspath 中的 JAR 原样放入 lib/，不包含 compileOnly 依赖
 * 默认输出到 build/libs/plugin-&lt;插件 ID&gt;-&lt;插件版本&gt;.spmod
 * 中间 JAR 由 jar 任务生成到 build/intermediates/spmod/
 */
@DisableCachingByDefault(because = "Archive tasks are not worth caching")
public abstract class WorkshopPluginTask extends Zip {
    /**
     * 用于产物命名的插件 ID，由 spmod.PluginId 提供
     */
    @Input
    public abstract Property<String> getPluginId();

    /**
     * 用于产物命名的插件版本，由 spmod.PluginVersion 提供
     */
    @Input
    public abstract Property<String> getPluginVersion();

    @Inject
    protected abstract ArchiveOperations getArchiveOperations();

    public WorkshopPluginTask() {
        setGroup("build");
        setDescription("将插件及运行时依赖打包为 .spmod 分发包");

        getArchiveBaseName().set(getPluginId().map(id -> "plugin-" + id));
        getArchiveVersion().set(getPluginVersion());
        getArchiveExtension().set("spmod");
        getDestinationDirectory().set(getProject().getLayout().getBuildDirectory().dir("libs"));

        TaskProvider<Jar> mainJar = getProject().getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class);
        into("classes", spec -> spec.from(
                getArchiveOperations().zipTree(mainJar.flatMap(Jar::getArchiveFile))
        ));

        Configuration runtimeClasspath = getProject().getConfigurations()
                .getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
        into("lib", spec -> spec.from(runtimeClasspath.filter(file -> file.getName().endsWith(".jar"))));
    }
}
