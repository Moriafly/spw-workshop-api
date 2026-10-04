# 字节码转换

插件可在**运行时替换宿主已加载类的字节码**，实现类似 Minecraft coremod 的深度定制。插件停用或卸载后，宿主移除转换器并再次重转换，目标类恢复为原始字节码。

这是最高危能力，需声明 `CLASS_TRANSFORM` 权限并在启用时由用户勾选授权，未授权时转换器不会被应用（声明方式见 [插件权限](permissions.md)）。

## 快速上手

扩展 `BytecodeTransformExtensionPoint` 提供转换器：

```kotlin
class AsmPlugin : Plugin() {
    @Extension
    class TransformExtension : BytecodeTransformExtensionPoint {
        // 持有稳定实例：宿主按插件 ID + 转换器实例去重，每次新建会导致重复重转换
        private val transformers = listOf(PlaybackControllerTransformer())

        override fun getTransformers(): List<HostClassTransformer> = transformers
    }
}
```

转换契约只传递字节码（`transform(className, byte[]): byte[]?`），插件可自由选用字节码库。推荐使用 **ByteBuddy `@Advice`**：增强逻辑写在普通 Java/Kotlin 类中，由 javac 编译期检查，不用逐条写字节码指令：

```java
public class PlayPauseAdvice {

    @Advice.OnMethodEnter
    public static void enter(@Advice.Origin("#m") String method) {
        System.out.println("[asm] PlaybackController." + method + "()");
    }
}
```

转换器只负责"应用到哪个类的哪些方法"：

```kotlin
class PlaybackControllerTransformer : HostClassTransformer {
    override val targetClassName = "com.xuncorp.spw.sample"

    override fun transform(className: String, classfileBuffer: ByteArray): ByteArray {
        // 目标类的类型层级经应用类加载器解析；
        // WorkshopApi 由宿主加载，其类加载器即应用类加载器
        val typePool = TypePool.Default.of(WorkshopApi::class.java.classLoader)
        return ByteBuddy()
            .rebase<Any>(
                typePool.describe(className).resolve(),
                // 原始字节来自宿主回调，避免从插件视角重新定位宿主类
                ClassFileLocator.Simple.of(className, classfileBuffer)
            )
            .visit(
                Advice.to(PlayPauseAdvice::class.java)
                    .on(
                        ElementMatchers.named<MethodDescription>("play")
                            .or(ElementMatchers.named<MethodDescription>("pause"))
                    )
            )
            .make()
            .bytes
    }
}
```

依赖 `implementation("net.bytebuddy:byte-buddy:1.18.14")` 会打进插件 `lib/`，由插件类加载器独享，宿主无需感知。

## 字节码库选择

- **ByteBuddy `@Advice`（推荐）**：如上，真实代码、编译期检查、活跃维护
- **Javassist**：以源码字符串改写（`insertBefore` 等），轻量但无编译期检查，对新版本 class 文件的支持存在不确定性
- **裸 ASM**：宿主经 API 传递，`compileOnly` 依赖本库即可使用，无需打包进 `lib/`；适合极简修改。方法入口注入等栈中性修改用 `ClassWriter(reader, ClassWriter.COMPUTE_MAXS)` 配合 `ClassReader.EXPAND_FRAMES`；若必须使用 `COMPUTE_FRAMES`，需覆写 `ClassWriter.getCommonSuperClass` 改用应用类加载器解析宿主类

## 约束

- **只能修改方法体等内容**（JVM 强制）：不可增删或改名字段/方法，不可修改签名、父类或接口。违反时重转换失败并记录日志，修改不会生效。Advice 内联不引入新成员，天然满足
- **类加载边界**：注入或内联的字节码只能引用目标类的类加载器（应用类加载器）可见的类——JDK、宿主类与 `WorkshopApi` 等宿主提供的 API；**不可引用插件自身的类**，否则目标类运行时抛出 `NoClassDefFoundError`
- suspend 函数编译为状态机，不适合作为转换目标
- 同一类被多个插件转换时按注册顺序链式应用，后注册的收到前一个转换后的字节
- Release 构建中宿主类名被 ProGuard 混淆，转换目标以宿主保留的稳定类为准；目标类找不到时转换不会生效
- Compose 界面：顶层 `@Composable` 函数编译为文件外观类（如 `ModManagementScreenKt`）中的静态方法；注意重组会重复进入方法。注入插件组件时使用 [Compose UI Hook 桥接](ui-hooks.md)，并保留原有组合分组与跳过逻辑
