# SPW 创意工坊（Mod）API

[![JitPack](https://jitpack.io/v/Moriafly/spw-workshop-api.svg)](https://jitpack.io/#Moriafly/spw-workshop-api)

SPW Workshop API 为 Salt Player 插件提供公开接口与扩展点，基于 PF4J，目前仍处于试验阶段。本仓库包含 API、Gradle 打包插件和可运行的示例工程。

## 开发文档

插件开发指南与使用示例统一维护在 [Salt Player 官网](https://saltplayer.com/workshop/)。首次开发请从 [开发入门](https://saltplayer.com/workshop/getting-started) 开始，按步骤配置开发环境、Gradle 仓库和插件解析规则，再编写、打包和安装插件。

| 文档 | 内容 |
| --- | --- |
| [开发入门](https://saltplayer.com/workshop/getting-started) | 开发环境、Gradle 接入、JitPack 插件解析、主类、扩展点与元数据 |
| [插件配置](https://saltplayer.com/workshop/configs) | 配置界面、用户设置的读写与变更监听 |
| [插件权限](https://saltplayer.com/workshop/permissions) | 权限声明、查询、失败处理与授权生命周期 |
| [直接 Hook API](https://saltplayer.com/workshop/hook) | 方法 Hook、UI 替换与追加、参数读取及生命周期 |
| [安装与管理 Mod](https://saltplayer.com/workshop/usage) | 本地导入、启用、配置与更新 |
| [发布 Mod](https://saltplayer.com/workshop/publishing) | GitHub 分享与 Steam 创意工坊发布 |

## 仓库内容

| 目录 | 内容 |
| --- | --- |
| [api](api) | `com.xuncorp.spw.workshop.api` 包中的公开契约与扩展点 |
| [gradle-plugin](gradle-plugin) | `com.xuncorp.spw.workshop` Gradle 插件，用于生成 `.spmod` 分发包 |
| [example](example) | Kotlin 插件示例与该工程的构建说明 |

构建本仓库的示例插件，请阅读 [example/README.md](example/README.md)。

请为自己的 GitHub 插件仓库添加 [salt-player-plugins](https://github.com/topics/salt-player-plugins) topic，方便用户查找插件。

## 许可证

本项目使用 Apache-2.0 许可证，详见 [LICENSE](LICENSE)。
