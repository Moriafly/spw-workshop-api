# SPW 示例插件

本示例展示 SPW Workshop API 的插件主类、播放扩展、曲库查询与配置管理。插件接入与功能使用说明见 [Salt Player 官网开发文档](https://saltplayer.com/workshop/getting-started)。

## 项目结构

```text
example/
├── src/main/kotlin/com/gg/example/
│   ├── MainPlugin.kt                 # 插件主类
│   ├── PlaybackExtensionExample.kt   # 播放扩展示例
│   ├── LibraryExample.kt             # 曲库查询示例
│   └── ConfigExample.kt              # 配置管理示例
├── src/main/resources/
│   └── preference_config.json        # 配置界面定义
├── build.gradle.kts                  # 构建配置
└── README.md
```

## 构建与运行

先按照官网 [开发环境与版本](https://saltplayer.com/workshop/getting-started#开发环境与版本) 配置开发环境，再在 **API 仓库根目录** 执行对应命令。

Windows 使用以下命令。

```powershell
.\gradlew.bat :example:plugin
```

Linux 使用以下命令。

```sh
./gradlew :example:plugin
```

构建产物位于 `example/build/libs/plugin-com.gg.example-1.0.0.spmod`。按照官网 [本地导入](https://saltplayer.com/workshop/usage#本地导入) 步骤安装并启用该插件。

## 相关资源

- [官网开发文档](https://saltplayer.com/workshop/getting-started)
- [API 源码](../api)
- [问题反馈](https://github.com/Moriafly/spw-workshop-api/issues)

本示例使用 Apache-2.0 许可证，详见仓库根目录的 [LICENSE](../LICENSE)。
