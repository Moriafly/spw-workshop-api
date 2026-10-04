# dev22 直接 Hook API

API 与打包插件版本为未发布的 `0.1.0-dev22`，插件在 `start()` 中通过 `WorkshopApi.hooks` 注册，Java 对应 `WorkshopApi.hooks()`

在 `spmod {}` 中声明 `PluginPermissions = listOf(PluginPermission.CLASS_TRANSFORM)`，并由用户授权后启用插件。宿主负责识别实际插件、解析目标、安装字节码桥接与清理生命周期，不需要声明转换扩展或提供 pluginId、hookId

## 普通方法

Kotlin 的 before、replace、after 可以组合使用：

```kotlin
override fun start() {
    WorkshopApi.hooks.hookMethod(
        className = "com.example.SomeClass",
        methodName = "calculate"
    ) {
        before { call -> call.args[0] = 100 }
        replace { call -> call.invokeOriginal() }
        after { call -> println(call.result) }
    }
}
```

`replace` 是 before 阶段执行回调并设置结果的 DSL 简写。before 已设置结果或异常时跳过 replace，replace 抛出的异常作为目标方法此次调用的异常交给 after，不再自动执行原方法

Java 使用明确的回调类型，无需 Kotlin 函数类型：

```java
@Override
public void start() {
    WorkshopApi.hooks().hookMethod(
        "com.example.SomeClass",
        "calculate",
        new MethodHook() {
            @Override
            public void before(MethodHookParam call) throws Throwable {
                call.getArgs()[0] = 100;
                call.setResult(call.invokeOriginal());
[ui-hooks.md](ui-hooks.md)            }

            @Override
            public void after(MethodHookParam call) {
                System.out.println(call.getResult());
            }
        }
    );
}
```

| 契约 | 行为 |
| --- | --- |
| `MethodHook()` / `MethodHook(int priority)` | 默认优先级 50，高优先级先进入 before |
| `MethodHookParam.method` / `getMethod()` | 实际被 Hook 的 `java.lang.reflect.Method` |
| `thisObject` / `getThisObject()` | 实例接收者，静态方法为 null |
| `args` / `getArgs()` | 实际参数数组，基本类型装箱，可在 before 中修改 |
| `result` / `getResult()` | 当前结果 |
| `throwable` / `getThrowable()` | 当前异常 |
| `setResult(value)` | before 中跳过原方法与后续 before，after 中修改最终结果，同时清除异常 |
| `setThrowable(failure)` | 设置最终异常，同时清除结果 |
| `invokeOriginal()` | 使用当前参数调用原方法，绕过此次入口的全部 Hook，不自动设置结果 |

`setResult(null)` 也是明确返回；void 方法接受 null 或 Kotlin Unit。结果与参数必须符合目标 JVM 类型。上下文仅在此次同步调用期间有效，不能缓存或转交其他线程

多插件共享方法桥接。before 按优先级从高到低执行，同优先级按注册顺序；after 只对已进入的 Hook 反向执行。before/after 意外抛出异常时记录日志并恢复该回调之前的参数数组和结果状态，对象内部修改和外部副作用无法回滚。原方法异常保持原类型，可由 after 修改；原方法内部的正常递归仍触发 Hook

## 定位与 Java 重载

`parameterTypes = null` 表示匹配唯一方法或调用；空列表表示无业务参数；其他列表使用 JVM 类型名，例如 `long`、`java.lang.String`、数组的 `Class.getName()` 结果，不使用 descriptor

```java
HookHandle hookMethod(String className, String methodName, MethodHook callback);
HookHandle hookMethod(String className, String methodName, List<String> parameterTypes, MethodHook callback);

HookHandle replaceComposable(String className, String methodName, String inClass, ComposableHook content);
HookHandle afterComposable(String className, String methodName, String inClass, ComposableHook content);

<S> HookHandle appendContent(
    String className, String methodName, String inClass, String parameter,
    Class<S> scopeType, ContentHook<S> content
);
```

UI 三种注册还提供完整重载，在回调前增加 `List<String> parameterTypes` 和 `int priority`，默认分别为 null 和 50。普通方法的优先级来自 MethodHook 构造函数；Kotlin DSL 使用 `priority` 参数

`inClass` 指调用所在的宿主类，扫描该类全部方法，包括编译器生成的 lambda 方法。调用缺失、歧义或签名不支持时抛出 IllegalArgumentException；安装失败抛出 IllegalStateException；无法识别插件或缺少权限时抛出 PluginPermissionDeniedException

## 替换歌词组件

```kotlin
WorkshopApi.hooks.replaceComposable(
    className = "com.xuncorp.voxzen.ui.player.component.lyrics.PlayerLyricsKt",
    methodName = "PlayerLyrics",
    inClass = "com.xuncorp.voxzen.ui.player.PlayerScreenKt"
) { call ->
    AccompanistLyrics.Content(
        modifier = call.argument("modifier"),
        activated = call.argument("activated")
    )
}
```

Kotlin DSL 自动包装为 `ComposableHook`。Java 注册 Kotlin 编写的组件实例：

```kotlin
class LyricsHook : ComposableHook() {
    @Composable
    override fun Content(call: UiHookCall) {
        AccompanistLyrics.Content(
            modifier = call.argument("modifier"),
            activated = call.argument("activated")
        )
    }
}
```

```java
WorkshopApi.hooks().replaceComposable(
    "com.xuncorp.voxzen.ui.player.component.lyrics.PlayerLyricsKt",
    "PlayerLyrics",
    "com.xuncorp.voxzen.ui.player.PlayerScreenKt",
    new LyricsHook()
);
```

Composable 内容需要启用 Compose compiler 的 Kotlin 源码，Java 无需传递 Composer。替换采用最高优先级注册，同优先级采用最早注册；无有效注册时执行原组件。`afterComposable` 采用同样定位与回调形式，在所选组件调用后按优先级绘制全部追加内容

保留 `call.argument<Modifier>("modifier")`，即可沿用宿主布局与动画。注入逻辑使用目标调用的 Composer，并由宿主维护组合分组和注册身份，切换注册时清理旧 Effect，普通进度更新保留组件状态

## 向菜单追加下一首

```kotlin
WorkshopApi.hooks.appendContent<PopupMenuScope>(
    className = "com.xuncorp.voxzen.ui.component.BlurKt",
    methodName = "BlurPopupMenu",
    inClass = "com.xuncorp.voxzen.ui.player.popup.PlayerMoreMenuFlyoutKt",
    parameter = "content"
) { call -> NextTrackMenuItem.append(this, call) }
```

```kotlin
object NextTrackMenuItem {
    fun append(scope: PopupMenuScope, call: UiHookCall) {
        val dismiss = call.argument<(() -> Unit)?>("onDismissRequest")
        scope.item(
            onClick = {
                dismiss?.invoke()
                WorkshopApi.playback.next()
            },
            content = { PopupMenuItem(text = "下一首（插件）") }
        )
    }
}
```

Java 使用同一个构建回调：

```java
WorkshopApi.hooks().appendContent(
    "com.xuncorp.voxzen.ui.component.BlurKt",
    "BlurPopupMenu",
    "com.xuncorp.voxzen.ui.player.popup.PlayerMoreMenuFlyoutKt",
    "content",
    PopupMenuScope.class,
    (scope, call) -> NextTrackMenuItem.INSTANCE.append(scope, call)
);
```

原构建器先运行，随后按优先级追加插件内容。ContentHook 是普通回调，只支持普通 Function1 构建器；应把 Composable 内容传给菜单 DSL 的 content 参数，不能直接在构建回调中调用 Composable。执行时验证 scopeType，签名或作用域不匹配明确报错

## UI 上下文与状态

`UiHookCall.thisObject` 为目标调用的实例接收者，静态调用为 null。`arguments` / `getArguments()` 是不可修改的浅拷贝，仅包含业务参数，基本类型自动装箱，不含 Composer、changed 或 default mask

可以按索引读取，或者用 `getArgument(String name)`、`getArgument(String name, Class<T> type)` 与 Kotlin `argument<T>(name)` 按名称读取。名称来自宿主 Kotlin Metadata；Java 方法的名字依赖 `-parameters` 编译信息。拿到的是该调用传入的值，不应缓存到当前组件或构建器生命周期之外

Hook 连接调用位置，持续更新仍依赖 Compose 可观察状态：Flow 使用 collectAsState，异步工作使用 LaunchedEffect，清理使用 DisposableEffect。歌词库通过 snapshotFlow 读取进度时，应先把进度采样到 mutableIntStateOf，再传入读取该状态的回调；仅读取 StateFlow.value 或系统时间不会触发 Snapshot 更新。不要使用 key(position) 在每次进度变化时重建组件

UI 回调异常按原 UI 异常路径传播，不隐式吞掉异常

## 默认参数

宿主默认表达式可能读取 CompositionLocal 或产生副作用，桥接不会复制或猜测这些表达式

- replaceComposable 在任一 default mask 非零时执行原组件，所有业务参数显式传入时才替换
- appendContent 的内容参数为 null 时执行原构建器路径，包括省略默认内容参数的情况；显式传入内容时可以追加，即使其他参数使用默认值
- afterComposable 在原组件或替换组件之后执行，业务参数仍取调用点传入值；省略默认参数时，arguments 中可能是 JVM 占位值，应只读取已确认显式传入的参数

宿主解析实际目标声明的 changed/default mask 布局，包括 @JvmStatic 的隐式接收者槽位。当前基线为宿主 Kotlin / Compose compiler 2.4.20、Compose Multiplatform 1.12.1，宿主升级编译器后需要重新验证参数边界

## 生命周期、依赖与边界

start 中的注册先准备，启动成功后统一安装；失败时回滚。已启动插件也可直接注册，成功返回表示安装完成。返回的 HookHandle 提供幂等 unhook 与 close，即使不保存句柄也会自动清理

停用、卸载或撤销 CLASS_TRANSFORM 后不开始新回调，已经进入的回调可以结束。归属绑定实际 PluginWrapper 与本次启用周期，旧句柄不能影响同 ID 的新插件。宿主保留无插件回调的 UI 桥接以继续观察注册变化，显示行为回退为原组件

Compose、Kotlin、协程、Workshop API 与宿主 UI 类型使用 compileOnly，不在 spmod 中重复打包。Composable 插件应使用与宿主一致的 Compose compiler / Kotlin 版本。插件可以正常调用自身私有代码，注入宿主的字节码只引用宿主桥接

首版支持宿主加载器可见的普通 JVM 方法与上述 UI 调用，不支持构造函数、native/abstract、suspend 状态机及已内联调用。目标名称依赖宿主版本，不承诺适配任意混淆或宿主升级

其他已发布 API 保持兼容，旧宿主的 hooks 默认实现抛出 UnsupportedOperationException。未发布的 UI 草案与低层字节码入口已经移除：UiHookExtensionPoint、UiHookContext、UiHookTransformers、BytecodeTransformExtensionPoint、HostClassTransformer 和 WorkshopApi.Ui 的绘制桥接均不保留别名，插件统一在 start 中通过 hooks 注册

权限沿用 CLASS_TRANSFORM / class-transform，以保留已有声明与授权；这是 Hook 的权限标识，不再提供插件字节码转换器入口。使用旧开发草案的插件需要迁移到本文的 Hook 用法
