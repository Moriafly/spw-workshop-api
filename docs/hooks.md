# Hook API 使用指南

通过 `WorkshopApi.hooks` 注册普通方法 Hook 或修改 UI，Java 对应 `WorkshopApi.hooks()`。Hook API 从 `0.1.0-dev22` 起提供，目前标记为 `UnstableSpwWorkshopApi`

## 开始使用

在插件的 `spmod {}` 配置中声明权限，并由用户授权：

```kotlin
spmod {
    PluginPermissions = listOf(PluginPermission.CLASS_TRANSFORM)
}
```

注册前可通过 `WorkshopApi.manager.isPermissionGranted(PluginPermission.CLASS_TRANSFORM)` 检查权限

下方的 `com.myapp` 名称仅用于说明调用方式，使用时应替换为目标宿主的实际类名和方法名

## 普通方法 Hook

`hookMethod` 支持三种回调，可按需要组合：

- `before`：在原方法之前执行，可以读取或修改参数，也可以提前返回
- `replace`：用回调返回值替换原方法结果，需要时通过 `invokeOriginal()` 调用原方法
- `after`：在方法完成后执行，可以读取或修改结果、处理异常

以下以 `calculate(Int): Int` 为目标：

```kotlin
override fun start() {
    WorkshopApi.hooks.hookMethod(
        className = "com.myapp.Calculator",
        methodName = "calculate",
        parameterTypes = listOf("int")
    ) {
        before { call ->
            call.args[0] = 100
        }
        replace { call ->
            call.invokeOriginal()
        }
        after { call ->
            println(call.result)
        }
    }
}
```

执行顺序为 `before → replace 或原方法 → after`。before 调用 `setResult()` 或 `setThrowable()` 后，会跳过 replace 和原方法；未配置 replace 时，默认执行原方法

Java 通过 `MethodHook` 声明 before 和 after，在 before 中调用 `setResult()` 即可替换返回值：

```java
@Override
public void start() {
    WorkshopApi.hooks().hookMethod(
        "com.myapp.Calculator",
        "calculate",
        List.of("int"),
        new MethodHook() {
            @Override
            public void before(MethodHookParam call) throws Throwable {
                call.getArgs()[0] = 100;
                call.setResult(call.invokeOriginal());
            }

            @Override
            public void after(MethodHookParam call) {
                System.out.println(call.getResult());
            }
        }
    );
}
```

### 读取参数和处理结果

| Kotlin / Java | 用途 |
| --- | --- |
| `method` / `getMethod()` | 被 Hook 的方法信息 |
| `thisObject` / `getThisObject()` | 当前实例，静态方法为 null |
| `args` / `getArgs()` | 本次调用的参数数组，基本类型自动装箱 |
| `result` / `getResult()` | 当前返回值 |
| `throwable` / `getThrowable()` | 当前异常 |
| `setResult(value)` | 设置返回值，并清除当前异常 |
| `setThrowable(failure)` | 设置异常，并清除当前返回值 |
| `invokeOriginal()` | 使用当前参数调用原方法，并返回调用结果 |

before 中修改 `args` 会影响原方法收到的参数；after 中调用 `setResult()` 或 `setThrowable()` 会修改最终结果。`setResult(null)` 也表示明确返回，void 方法可使用 null 或 Kotlin Unit

`invokeOriginal()` 不会再次触发本次入口的 Hook，也不会自动设置返回值。原方法内部的递归调用仍可触发 Hook

参数和返回值必须符合目标方法的类型。`MethodHookParam` 只在本次同步调用期间有效，不应保存或传给其他线程；回调在目标方法的调用线程执行

### 优先级和异常

默认优先级为 50，Kotlin 通过 `priority` 参数设置，Java 使用 `new MethodHook(priority)`。before 按优先级从高到低执行，同优先级按注册顺序；after 对已经进入的 Hook 按相反顺序执行。提前返回会停止后续 before

replace 或原方法抛出的异常可以在 after 中读取和处理。before/after 自身意外抛出异常时，该回调对参数数组和结果的修改不生效，但对象内部修改和其他副作用不能撤销

## 选择目标

| 参数 | 含义 |
| --- | --- |
| `className` | 声明目标方法的类的完整名称 |
| `methodName` | 目标方法名称 |
| `parameterTypes` | 用于区分同名方法的参数类型列表 |
| `inClass` | UI 调用所在的类，修改仅作用于该类中的目标调用 |

`parameterTypes` 默认为 null，要求目标唯一；空列表表示没有业务参数。类型使用完整名称，如 `java.lang.String`，基本类型使用 `int`、`long` 等名称，数组使用 `Class.getName()` 的结果。UI Hook 只填写业务参数类型

UI Hook 要求 `inClass` 中存在唯一匹配的目标调用。目标名称和签名可能随宿主版本变化，应明确插件支持的宿主版本

## 替换或追加 Composable

`replaceComposable` 替换所选组件，`afterComposable` 在所选组件正常返回后追加内容

```kotlin
WorkshopApi.hooks.replaceComposable(
    className = "com.myapp.ui.PanelKt",
    methodName = "Panel",
    inClass = "com.myapp.ui.ScreenKt"
) { call ->
    PluginPanel(title = call.argument("title"))
}
```

这里的 `PluginPanel` 是插件自行编写的 `@Composable` 函数。需要在组件之后执行副作用时，使用 Effect，避免重组时重复执行：

```kotlin
WorkshopApi.hooks.afterComposable(
    className = "com.myapp.ui.PanelKt",
    methodName = "Panel",
    inClass = "com.myapp.ui.ScreenKt"
) {
    LaunchedEffect(Unit) {
        WorkshopApi.ui.toast("我被Hook了", WorkshopApi.Ui.ToastType.Success)
    }
}
```

Java 可以注册 Kotlin 编写的 `ComposableHook`：

```kotlin
class PanelHook : ComposableHook() {
    @Composable
    override fun Content(call: UiHookCall) {
        PluginPanel(title = call.argument("title"))
    }
}
```

```java
WorkshopApi.hooks().replaceComposable(
    "com.myapp.ui.PanelKt",
    "Panel",
    "com.myapp.ui.ScreenKt",
    new PanelHook()
);
```

Composable 内容使用启用 Compose compiler 的 Kotlin 编写。UI Hook 的默认优先级为 50，Kotlin 可传入 `priority`；Java 的完整重载在回调前接收 `parameterTypes` 和 `priority`

同一调用位置有多个替换时，采用最高优先级注册，同优先级采用最早注册；afterComposable 按优先级执行全部追加内容。没有有效替换时，显示原组件

## 追加构建内容

`appendContent` 用于向普通构建回调追加内容，例如菜单。`parameter` 指定要追加的构建参数，作用域类型应与目标组件一致

假设目标菜单提供 `MenuScope` 作用域，插件通过自行编写的 `PluginMenuItems.append()` 构建菜单项：

```kotlin
WorkshopApi.hooks.appendContent<MenuScope>(
    className = "com.myapp.ui.MenuKt",
    methodName = "Menu",
    inClass = "com.myapp.ui.ScreenKt",
    parameter = "content"
) { call ->
    PluginMenuItems.append(this, call)
}
```

Java 显式传入作用域类型：

```java
WorkshopApi.hooks().appendContent(
    "com.myapp.ui.MenuKt",
    "Menu",
    "com.myapp.ui.ScreenKt",
    "content",
    MenuScope.class,
    (scope, call) -> PluginMenuItems.INSTANCE.append(scope, call)
);
```

原构建回调先执行，随后按优先级追加插件内容。这里是普通构建回调，不能直接调用 Composable；需要绘制组件时，将 Composable 内容传入目标作用域提供的内容参数。appendContent 不支持直接追加到 Composable lambda

## UI 参数与状态

`UiHookCall` 提供当前调用的业务参数：

```kotlin
val title = call.argument<String>("title")
val firstArgument = call.argument<Any?>(0)
```

```java
String title = call.getArgument("title", String.class);
Object firstArgument = call.getArgument(0);
```

`arguments` / `getArguments()` 是不可修改的浅拷贝，基本类型自动装箱；`thisObject` / `getThisObject()` 是当前实例，静态调用为 null。按名称读取要求目标提供参数名，缺少名称时使用索引

参数仅在当前组件或构建器生命周期内有效。状态更新遵循 Compose 的用法：Flow 使用 `collectAsState()`，异步工作使用 `LaunchedEffect`，需要释放资源时使用 `DisposableEffect`。UI 回调抛出的异常会正常向上传播

### 默认参数注意事项

- `replaceComposable` 只替换业务参数全部显式传入的调用，使用默认参数的调用保持原组件
- `afterComposable` 中应只读取已经确认显式传入的参数，不将省略的参数当作最终默认值
- `appendContent` 要求目标调用显式提供构建回调，构建参数为 null 或被省略时不追加

## 注销与生命周期

注册方法返回 `HookHandle`，需要提前取消时调用 `unhook()` 或 `close()`，重复调用是安全的：

```kotlin
val handle = WorkshopApi.hooks.hookMethod(
    className = "com.myapp.Calculator",
    methodName = "calculate"
) {
    after { call -> println(call.result) }
}

handle.unhook()
```

插件停用、卸载或撤销权限后，注册会自动清理，无需为自动清理保存句柄。已经开始的回调可以完成；插件重新启用后，应重新注册所需 Hook

## 常见限制与错误

普通方法 Hook 不支持构造函数、native、abstract、suspend 方法和已内联的调用。Compose、Kotlin 和 Workshop API 等宿主提供的依赖使用 `compileOnly`，UI 插件应与宿主的 Kotlin / Compose compiler 版本保持一致

| 异常 | 检查方向 |
| --- | --- |
| `PluginPermissionDeniedException` | 是否声明并获得 CLASS_TRANSFORM 权限，是否从插件中注册 |
| `IllegalArgumentException` | 目标是否存在、是否唯一，参数类型、参数名称或作用域是否匹配 |
| `IllegalStateException` | Hook 注册是否成功，结合错误信息检查目标和宿主版本 |
