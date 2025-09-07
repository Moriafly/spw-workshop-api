# SPW 示例插件

这是一个功能完整的 SPW (Salt Player for Windows) 插件示例，展示了如何使用 SPW Workshop API 进行插件扩展。

## 📁 项目结构

```
example-plugin/
├── src/main/kotlin/com/gg/example/
│   ├── MainPlugin.kt                 # 插件主类
│   ├── PlaybackExtensionExample.kt   # 播放扩展示例
│   ├── UIHookExample.kt              # UI 扩展示例
│   ├── ConfigExample.kt              # 配置管理示例
├── src/main/resources/
│   ├── preference_config.json        # 默认配置文件
├── build.gradle.kts                  # 构建配置
└── README.md                         # 说明文档
```

### 构建插件

1. 克隆项目到本地
2. 在项目根目录执行构建命令：

```bash
./gradlew plugin
```

3. 构建完成后，插件文件会自动复制到 SPW 插件目录

### 安装插件

1. 确保 SPW 已正确安装
2. 将构建好的插件文件复制到：
   ```
   %APPDATA%/Salt Player for Windows/workshop/plugins/
   ```
3. 重启 SPW
4. 在 设置 → 创意工坊 → 模组管理 中启用插件

## 📄 许可证

本项目采用 Apache-2.0 许可证，详见 [LICENSE](./LICENSE) 文件。

## 🔗 相关链接

- [SPW Workshop API 文档](https://github.com/Moriafly/spw-workshop-api)
- [问题反馈](https://github.com/Moriafly/spw-workshop-api/issues)