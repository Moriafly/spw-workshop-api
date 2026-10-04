# Compose UI Hook

从 API `0.1.0-dev22` 开始，插件可提供 Compose 内容，并用 `UiHookTransformers` 选择宿主调用位置。此能力沿用 `CLASS_TRANSFORM` 声明和用户授权；未声明、未授权或未启动的插件不会注册内容

## 选择辅助方法

| 方法 | 用途 | 插件实现 |
| --- | --- | --- |
| `afterCall` | 在指定调用后绘制额外组件 | `Content(context)` |
| `replaceCall` | 替换一个 Composable 调用，无有效 Hook 时执行原组件 | `Content(context)` |
| `appendContent` | 包装普通 `Function1` DSL 构建器，先构建原内容，再添加插件内容 | `buildContent(context)` |

转换器实例应在拓展中持有并复用，避免每次 `getTransformers()` 创建新对象引起重复重转换

## 替换歌词组件

```kotlin
@Extension
class LyricsExtension : UiHookExtensionPoint {
    override val hookId: String = "accompanist-lyrics"

    @Composable
    override fun Content(context: UiHookContext) {
        val modifier = context.arguments[0] as Modifier
        val activated = context.arguments[1] as Boolean
        MyLyrics(modifier = modifier, activated = activated)
    }
}

@Extension
class TransformExtension : BytecodeTransformExtensionPoint {
    private val transformers = listOf(
        UiHookTransformers.replaceCall(
            targetClassName = "com.xuncorp.voxzen.ui.player.PlayerScreenKt",
            callOwner = "com.xuncorp.voxzen.ui.player.component.lyrics.PlayerLyricsKt",
            callMethodName = "PlayerLyrics",
            pluginId = "workshop-asm",
            hookId = "accompanist-lyrics"
        )
    )

    override fun getTransformers(): List<HostClassTransformer> = transformers
}
```

`replaceCall` 自动保存原参数，使用被替换调用的 Composer，并维护分支组合分组。`context.arguments` 中的 `Modifier` 包含宿主原来的布局和动画；替代组件应保留它。没有有效注册时执行原调用，停用和卸载会恢复原组件

`targetMethodName = null` 在该类全部方法中寻找唯一调用，包括 Compose compiler 提取出来的 lambda。例如 `PlayerScreen` 的实际歌词调用位于编译器生成的方法内，不能仅按源码中的函数名定位

## 向更多菜单追加下一首

```kotlin
@Extension
class NextTrackExtension : UiHookExtensionPoint {
    override val hookId: String = "next-track"

    @OptIn(UnstableSpcUiApi::class)
    override fun buildContent(context: UiHookContext) {
        val scope = context.contentScope as PopupMenuScope
        scope.item(
            onClick = { WorkshopApi.playback.next() },
            content = { PopupMenuItem(text = "Next track") }
        )
    }
}
```

对应的转换器：

```kotlin
UiHookTransformers.appendContent(
    targetClassName = "com.xuncorp.voxzen.ui.player.popup.PlayerMoreMenuFlyoutKt",
    targetMethodName = "PlayerMoreMenuFlyout",
    callOwner = "com.xuncorp.voxzen.ui.component.BlurKt",
    callMethodName = "BlurPopupMenu",
    contentArgumentIndex = 2,
    pluginId = "workshop-asm",
    hookId = "next-track"
)
```

`contentArgumentIndex` 是被调用方法 JVM 参数的索引，从 0 开始，不含实例接收者；这里的参数顺序为 `onDismissRequest`、`header`、`content`、Composer、changed、default mask

默认表达式由宿主函数求值，辅助方法不会在调用点复制默认值逻辑：

- `replaceCall` 检测到任一非零 default mask 时执行原调用；布局可确定且所有参数均显式传入（default mask 全为零）时仍可替换
- `appendContent` 在内容参数为 `null` 时执行原调用，包括省略默认内容参数的情况。显式传入内容时仍可追加，即使其他参数使用默认值；这些其他参数在 `context.arguments` 中仍是 JVM 占位值，插件应只读取已确认显式传入的参数

这也保留了可空内容参数的原有语义，以及无 Hook 或旧桥接实现下的正常回退。需要 Hook 默认内容时，应选择已显式传入内容的调用点

`buildContent` 是普通构建回调，不能直接调用 Composable，应把组件作为 DSL 的 `content` 参数传入。`appendContent` 仅支持普通 `Function1` 构建器，不支持 `@Composable` lambda；需按目标宿主版本核实具体作用域和参数类型

原构建器先执行，随后追加插件内容；回调与原构建器在同一线程运行，可能因宿主重组再次执行。不要在其中创建长期资源

## 编译器 ABI 边界

`replaceCall` 的默认参数解析以 **Kotlin / Compose compiler 2.3.0** 和 **Compose Multiplatform 1.11.0-alpha01** 为验证基线。Composer 之后必须是该 ABI 的整数 changed/default masks；明确不支持的布局会导致转换失败，不应用部分修改。changed mask 每个容纳 10 个参数（包括接收者），default mask 每个容纳 31 个值参数，不包含实例接收者

`@JvmStatic` 可能保留原实例接收者的 changed 槽位。静态调用在 10 个参数的倍数边界需要声明信息；转换器只使用本次输入字节码中的声明，无法确定时保留原调用。因此跨类调用在这一边界保守回退，即使所有参数均显式传入。转换器不为此读取 `callOwner` 的类资源

`appendContent` 仅要求唯一 Composer 参数和 Composer 之前指定的 `Function1` 内容参数，不校验 Composer 之后的 changed/default mask 布局。`afterCall` 也不解析默认参数

宿主升级 Kotlin / Compose compiler 时，应先检查这里的参数布局及默认参数回归测试，再确认 Hook 兼容性

## 上下文和响应式状态

`UiHookContext.arguments` 按被调用方法的 JVM 参数顺序排列，仅包含 Composer 前面的参数，不含实例接收者、Composer、changed 或 default mask，基本类型自动装箱。该列表为不可修改的浅拷贝，其中的对象和回调仍属于原调用

`appendContent` 额外提供原构建器的 `contentScope`；其他 Hook 的作用域为 null。`afterCall` 不捕获调用参数，收到 `UiHookContext.Empty`。上下文不能保存到当前组件或构建器生命周期之外

字节码注入负责接通调用，状态变化仍须由 Compose 可观察状态驱动。Flow 用 `collectAsState()` 订阅，异步工作用 `LaunchedEffect`，资源清理用 `DisposableEffect`

尤其是通过 `snapshotFlow { currentPosition() }` 观察播放进度的组件，其回调必须读取 Snapshot State。仅读取 `StateFlow.value` 或系统时间不会产生 Snapshot 变更通知；应先将进度采样到 `mutableIntStateOf`，再传入 `{ position.intValue }`。不要用 `key(position)` 反复重建组件，否则会重置滚动和动画状态

`hookId` 在插件内唯一且非空白，同一拓展实例的值保持不变。同一插件的重复 ID 与空白 ID 不注册，宿主记录错误；不同插件可使用相同 ID

## JVM 与依赖

插件必须启用 `org.jetbrains.kotlin.plugin.compose`，版本与 Kotlin 插件一致。Compose、Kotlin、Salt UI、SPC 等宿主提供的依赖使用 `compileOnly`，并选择与宿主实际运行时兼容的版本；不要将它们打进插件 `lib/` 形成第二套运行时。插件私有第三方组件库可放入 `lib/`，但要排除它们携带的宿主运行时

Java 可直接调用静态辅助方法：

```java
HostClassTransformer transformer = UiHookTransformers.replaceCall(
    "com.xuncorp.voxzen.ui.player.PlayerScreenKt",
    "com.xuncorp.voxzen.ui.player.component.lyrics.PlayerLyricsKt",
    "PlayerLyrics",
    "workshop-asm",
    "accompanist-lyrics"
);
```

Java 实现拓展时签名为 `String getHookId()`、`void Content(UiHookContext context, Composer composer, int changed)` 和 `void buildContent(UiHookContext context)`，后两个方法均有默认实现。编写 Composable 优先使用启用 Compose compiler 的 Kotlin；Java 直接调用编译后的组件也须遵守组合协议

- 类名使用二进制名，包名以 `.` 分隔，内部类使用 `$`
- `pluginId` 必须与 Manifest 的 `Plugin-Id` 一致，`hookId` 对应本插件拓展
- 传入 `targetMethodName` 时目标方法必须唯一；未传时在整类中匹配唯一调用
- 存在重载时用 `targetMethodDescriptor`、`callMethodDescriptor` 精确匹配，descriptor 包含编译器参数；同一位置多次匹配仍拒绝转换
- 目标缺失、匹配不唯一、类名不符或签名不支持时，转换抛出 `IllegalStateException`，宿主记录失败，不应用此次转换；空白字符串或负的内容索引在创建转换器时以 `IllegalArgumentException` 拒绝
- 实例接收者、long/double 参数、原参数表达式和返回值由辅助方法处理；`replaceCall` 要求被替换调用返回 void

## 自定义 ASM 桥接

注入的字节码只引用宿主可见的 API，宿主根据插件 ID 和 Hook ID 调用插件组件，不直接引用插件类

```kotlin
WorkshopApi.ui.renderHook("workshop-asm", "my-panel", context)
```

三参数入口的 JVM descriptor 为 `(Ljava/lang/String;Ljava/lang/String;Lcom/xuncorp/spw/workshop/api/ui/UiHookContext;Landroidx/compose/runtime/Composer;I)V`，owner 为 `com/xuncorp/spw/workshop/api/WorkshopApi$Ui`，使用 `INVOKEINTERFACE`。调用前通过 `WorkshopApi.ui()` 获取接收者，传入该调用位置当前使用的 Composer；常量 ID 可传 `changed = 0`

原有两参数 `renderHook(pluginId, hookId)` 入口仍保留，descriptor 为 `(Ljava/lang/String;Ljava/lang/String;Landroidx/compose/runtime/Composer;I)V`，使用空上下文

有效组合分支中的调用须维护分组平衡和原跳过逻辑。普通方法入口或出口 Advice 不保证有效组合位置，后台回调不能作为绘制入口

## 生命周期与限制

- 停用或卸载先移除内容注册，已挂载组件在后续组合中清理 Effect；重新启用、重新加载以及同帧停用再启用使用新的组件生命周期
- 不存在的 Hook 不绘制额外内容；替换模式恢复原组件，追加模式使用原构建器；旧 `WorkshopApi.Ui` 实现的新增 JVM default 方法也遵循此行为
- 字节码重转换本身不触发重组；首次安装 Hook 后重新进入目标页面，或由正常状态变化触发目标组合。已有桥接会观察注册变化并响应停用
- 同一 Hook 在多个位置调用时，各位置拥有独立组合状态，并继承该位置的 CompositionLocal 和布局环境
- 插件内容异常按普通组件异常传播；字节码还原不会撤销插件对全局对象或持久数据的副作用
- 桥接是公开契约，宿主内部类名、方法和作用域仍会随版本或混淆改变；插件需明确支持的宿主版本，匹配失败时报告错误

完整示例位于 Voxzen 的 `workshop-demo-asm` 模块，字节码约束见 [字节码转换](bytecode-transform.md)
