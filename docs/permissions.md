# 插件权限

## Gradle 声明

Kotlin DSL：

```kotlin
import com.xuncorp.spw.workshop.gradle.PluginPermission

spmod {
    PluginClass = "com.example.MyPlugin"
    PluginId = "com.example.my-plugin"
    PluginVersion = "1.0.0"
    PluginPermissions = listOf(PluginPermission.KEY_BINDINGS, PluginPermission.LIBRARY_READ)
}
```

Groovy：

```groovy
import com.xuncorp.spw.workshop.gradle.PluginPermission

spmod { config ->
    config.PluginClass = 'com.example.MyPlugin'
    config.PluginId = 'com.example.my-plugin'
    config.PluginVersion = '1.0.0'
    config.PluginPermissions = [PluginPermission.KEY_BINDINGS, PluginPermission.LIBRARY_READ]
}
```

`PluginPermissions` 的类型为 `List<PluginPermission>`，默认空列表，未设置或设为 `null` 表示不申请权限。列表只接受插件提供的枚举，重复项会去重；Kotlin DSL 传入字符串会产生脚本编译错误，Groovy 的动态列表包含字符串或 `null` 元素时会报告明确的配置错误

| Gradle 枚举 | Manifest 标识 | API 常量 | 能力 |
| --- | --- | --- | --- |
| `PluginPermission.KEY_BINDINGS` | `key-bindings` | `PluginPermission.KEY_BINDINGS` | 注册应用内快捷键，并可通过 `hasGlobal` 允许用户自行配置全局快捷键 |
| `PluginPermission.LIBRARY_READ` | `library-read` | `PluginPermission.LIBRARY_READ` | 查询曲库歌曲元数据、文件路径、收藏状态和内嵌封面，包括当前歌曲的元数据查询 |
| `PluginPermission.LIBRARY_WRITE` | `library-write` | `PluginPermission.LIBRARY_WRITE` | 写入曲库数据的独立权限；为后续接口预留，当前 API 尚无写入入口 |

读写权限互不包含。插件只应申请实际使用的权限；当前曲库查询只需声明 `LIBRARY_READ`，无需申请 `LIBRARY_WRITE`

## 查询与失败处理

`WorkshopApi.manager.isPermissionGranted(permission)` 同步查询实际调用插件的授权，无弹窗或加载副作用，可在任意线程调用。未声明、未授权、无法识别调用者或调用类加载器已经卸载时返回 `false`。缓存或传递 Manager 引用不会改变查询归属

Kotlin，在插件的 `start()` 中：

```kotlin
override fun start() {
    if (WorkshopApi.manager.isPermissionGranted(PluginPermission.KEY_BINDINGS)) {
        try {
            WorkshopApi.manager.keyBindingManager.register(
                actionId = "open-panel",
                title = "打开面板",
                defaultShortcut = null,
                handler = Runnable { openPanel() }
            )
        } catch (denied: PluginPermissionDeniedException) {
            // 保留插件的其他能力，停用快捷键功能
        }
    }
}
```

Java：

```java
@Override
public void start() {
    if (WorkshopApi.manager().isPermissionGranted(PluginPermission.KEY_BINDINGS)) {
        try {
            WorkshopApi.manager().getKeyBindingManager().register(
                    "open-panel",
                    "打开面板",
                    null,
                    false,
                    this::openPanel
            );
        } catch (PluginPermissionDeniedException denied) {
            // 保留插件的其他能力，停用快捷键功能
        }
    }
}
```

未声明或未经授权时，`register` 同步抛出 `PluginPermissionDeniedException`，异常的 `pluginId` 与 `permission` 标明被拒绝的插件及所需权限。该异常继承 `SecurityException`，Java 可通过 `getPluginId()`、`getPermission()` 读取。调用归属、注册时机和参数约束仍适用；注销与关闭注册句柄无需权限

## 曲库读取

`WorkshopApi.library` 的 `getTrackById`、`getAllTracks`、`getTracks`、`getCoverById` 均要求 `LIBRARY_READ`。权限不足时返回失败的 `CompletionStage`，不会启动数据读取。宿主在调用线程识别插件，在读取前与交付结果前重新校验权限和加载身份；缓存或向其他插件传递 Library 引用不会转移授权

Kotlin（运行时代码导入 `com.xuncorp.spw.workshop.api.PluginPermission`）：

```kotlin
if (WorkshopApi.manager.isPermissionGranted(PluginPermission.LIBRARY_READ)) {
    WorkshopApi.library.getTracks(afterId = null, limit = 20)
        .whenComplete { tracks, failure ->
            val cause = (failure as? java.util.concurrent.CompletionException)?.cause ?: failure
            when (cause) {
                null -> tracks.forEach { println(it.title) }
                is PluginPermissionDeniedException -> println("曲库读取权限不可用")
                else -> cause.printStackTrace()
            }
        }
}
```

Java：

```java
if (WorkshopApi.manager().isPermissionGranted(PluginPermission.LIBRARY_READ)) {
    WorkshopApi.library().getTracks(null, 20).whenComplete((tracks, failure) -> {
        Throwable cause = failure instanceof java.util.concurrent.CompletionException
                ? failure.getCause() : failure;
        if (cause == null) {
            tracks.forEach(track -> System.out.println(track.getTitle()));
        } else if (cause instanceof PluginPermissionDeniedException) {
            System.out.println("曲库读取权限不可用");
        } else {
            cause.printStackTrace();
        }
    });
}
```

主动查询权限只用于功能降级，不能替代失败处理。`Playback.getCurrentMediaItem()` 也需要该权限，即使当前没有歌曲：调用时未授权会同步抛出 `PluginPermissionDeniedException`，查询期间的权限拒绝通过 `CompletionException` 的 cause 返回。无法识别调用者或已卸载类发起的新调用也会拒绝，异常的 `pluginId` 为 `unknown`

停用插件保留授权，已接受的查询可以继续完成；交付结果前撤销授权或卸载插件会令查询失败。同 ID 插件重新加载后，旧调用与旧类不能复用新加载实例的权限。已经成功交付的快照不会被收回。取消转换出的 future 不保证取消底层读取

## 用户决定与生命周期

- 首次启用时，在宿主现有的启用弹窗中显示权限及用途，新权限默认不勾选。用户可以不授予权限而启用插件，插件应据查询结果保留可用功能；未处理的注册拒绝异常会导致启动失败
- 确认后保存授权与拒绝决定，再调用插件 `start()`；取消弹窗不改变已保存决定
- 重启、停用和普通更新保留决定。可停用插件后在启用弹窗中调整勾选
- 更新新增权限时暂停自动启动，等待用户重新确认；新权限不会自动授予。依赖链中存在待确认权限时也不会自动启动，需先确认相应依赖插件
- 未知权限在启用弹窗中标记为不支持，无法授予
- 删除本地插件或取消 Steam 订阅会清除决定，再次安装需要重新授权；卸载类加载器以更新插件时保留决定
- 插件必须为受保护的能力补充 Gradle 权限声明，并处理用户拒绝授权的情况

API 的查询方法提供返回 `false` 的 JVM 默认实现，以兼容旧的 Manager 实现类；这不使新消费者能够在缺少该方法的旧 API JAR 上运行

此机制控制 Workshop API 提供的宿主能力。当前插件仍在宿主 JVM 内运行，权限声明和检查不构成操作系统沙箱
