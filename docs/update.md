## 前言

该板块将介绍插件更新的相关设置
## 插件更新检查

插件可以通过在元数据中设置 `Plugin-Update-Url` 来启用自动更新功能。SPW 会定期访问该 URL 检查插件更新。

### 更新检查 JSON 格式

更新检查 URL 应返回以下格式的 JSON 数据：

```json
{
  "version": "1.2.3",
  "download_url": "https://example.com/plugin-update.zip",
  "changelog": "修复了若干问题\n新增了某某功能",
  "min_spw_version": "1.8.0",
  "release_date": "2025-09-12",
  "checksum": {
    "type": "sha256",
    "value": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  }
}
```

### 字段说明
```
version (必需): 新版本的版本号

download_url (必需): 新版本插件的下载链接

changelog (可选): 版本更新日志，支持多行文本

min_spw_version (可选): 需要的最低 SPW 版本

release_date (可选): 发布日期，格式为 YYYY-MM-DD

checksum (可选): 文件校验信息

type: 校验算法类型（如 sha256, md5）

value: 校验值
```