# 插件权限

## Gradle 声明

Kotlin DSL：

```kotlin
import com.xuncorp.spw.workshop.gradle.PluginPermission

spmod {
    PluginClass = "com.example.MyPlugin"
    PluginId = "com.example.my-plugin"
    PluginVersion = "1.0.0"
    PluginPermissions = listOf(PluginPermission.KEY_BINDINGS)
}
```

Groovy：

```groovy
import com.xuncorp.spw.workshop.gradle.PluginPermission

spmod { config ->
    config.PluginClass = 'com.example.MyPlugin'
    config.PluginId = 'com.example.my-plugin'
    config.PluginVersion = '1.0.0'
    config.PluginPermissions = [PluginPermission.KEY_BINDINGS]
}
```

`PluginPermissions` 的类型为 `List<PluginPermission>`，默认空列表，未设置或设为 `null` 表示不申请权限。列表只接受插件提供的枚举，重复项会去重；Kotlin DSL 传入字符串会产生脚本编译错误，Groovy 的动态列表包含字符串或 `null` 元素时会报告明确的配置错误

| Gradle 枚举 | Manifest 标识 | API 常量 | 能力 |
| --- | --- | --- | --- |
| `PluginPermission.KEY_BINDINGS` | `key-bindings` | `PluginPermission.KEY_BINDINGS` | 注册应用内快捷键，并可通过 `hasGlobal` 允许用户自行配置全局快捷键 |

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

## 用户决定与生命周期

- 首次启用时，在宿主现有的启用弹窗中显示权限及用途，新权限默认不勾选。用户可以不授予权限而启用插件，插件应据查询结果保留可用功能；未处理的注册拒绝异常会导致启动失败
- 确认后保存授权与拒绝决定，再调用插件 `start()`；取消弹窗不改变已保存决定
- 重启、停用和普通更新保留决定。可停用插件后在启用弹窗中调整勾选
- 更新新增权限时暂停自动启动，等待用户重新确认；新权限不会自动授予。依赖链中存在待确认权限时也不会自动启动，需先确认相应依赖插件
- 未知权限在启用弹窗中标记为不支持，无法授予
- 删除本地插件或取消 Steam 订阅会清除决定，再次安装需要重新授权；卸载类加载器以更新插件时保留决定
- 旧插件未声明权限时没有快捷键权限，需要补充 Gradle 声明。旧 JVM 成员保持兼容，但权限检查是新增的行为约束

API 的查询方法提供返回 `false` 的 JVM 默认实现，以兼容旧的 Manager 实现类；这不使新消费者能够在缺少该方法的旧 API JAR 上运行

此机制控制 Workshop API 提供的宿主能力。当前插件仍在宿主 JVM 内运行，权限声明和检查不构成操作系统沙箱
