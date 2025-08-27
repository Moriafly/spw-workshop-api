# 配置文件

## 前言

该板块将介绍插件配置相关的方法及注意事项

## 元数据说明
在插件清单中，可以通过 Plugin-Has-Config 属性声明插件是否有配置文件：

- 值为 "true" 表示插件有配置文件
- 值为 "false" 表示插件没有配置文件（可选）

## preference_config.json 配置文件详解

`preference_config.json` 是用于定义插件配置界面的 JSON 配置文件，它允许插件开发者声明式地定义配置项，由SPW 来接管配置界面的渲染。

### 配置文件结构

配置文件采用 JSON 格式，包含一个 `configs` 数组，每个数组元素代表一个配置组：

```json
{
  "configs": [
    {
      "title": "组标题",
      "config": "配置文件路径",
      "preferences": [
        // 配置项数组
      ]
    }
  ]
}
```

### 配置组属性

每个配置组包含以下属性：

- `title` (必需): 配置组的显示标题
- `config` (必需): 对应的配置文件路径（%APPDATA%/workshop/data/plugin_id/）
- `preferences` (必需): 包含配置项的数组

### 配置项通用属性

所有配置项类型都支持以下属性：

- `type` (必需): 配置项类型（"switch", "list", "button", "seekbar", "edittext"）
- `title` (必需): 配置项的显示标题
- `summary` (可选): 配置项的摘要描述 (seekbar 类型目前不支持)
- `key` (必须): 配置项在配置文件中的键名（button 类型不需要）
- `default_value` (必须): 配置项的默认值 (button 类型不需要)

## 配置项类型

### 1. Switch (开关)
用于布尔值配置，显示为开关控件。
```json
{
  "key": "key_name",
  "type": "switch",
  "title": "Switch Title",
  "summary": "This is a summary",
  "default_value": false
}
```

### 2. List (列表)
用于多选一配置，点击后弹出选择框。
```json
{
  "key": "key_name",
  "type": "list",
  "title": "List Title",
  "summary": "This is a summary",
  "entries": ["eA", "eB", "eC"],
  "entry_values": ["A", "B", "C"],
  "default_value": "A"
}
```

额外属性: 
- `summary` (可选): 配置项的摘要描述
- `entries` (必需): 列表项的显示文本数组
- `entry_values` (必需): 列表项对应的值数组

### 3. Button (按钮)
用于触发操作，不保存配置值。
```json
{
  "type": "button",
  "title": "Button Title",
  "summary": "This is a summary",
  "arrow_type": "none",
  "on_click": "class.method"
}
```

额外属性: 
- `arrow_type` (可选): 箭头类型（"none", "link", "arrow"）
- `on_click` (必需): 点击时调用的静态方法全限定名 比如: `com.example.Plugin.onClick`

### 4. Seekbar (滑动条)
用于数值范围配置，显示为滑动条。
```json
{
  "key": "key_name",
  "type": "seekbar",
  "title": "Seekbar Title",
  "default_value": 10.0,
  "min": 1.0,
  "max": 100.0
}
```

额外属性: 
- `min` (必需): 最小值 (Float)
- `max` (必需): 最大值 (Float)

### 5. EditText (文本输入)
用于字符串配置，显示为带箭头的按钮，点击后将弹出dialog进行修改
```json
{
  "key": "key_name",
  "type": "edittext",
  "title": "Edittext Title",
  "summary": "This is a summary",
  "default_value": "Hello World"
}
```

### 示例配置
```json
{
    "configs": [
        {
            "title": "配置组标题",
            "config": "config.json",
            "preferences": [
                {
                    "key": "key_name",
                    "type": "switch",
                    "title": "开关标题",
                    "summary": "开关摘要",
                    "default_value": false
                },
                {
                    "key": "key_name",
                    "type": "list",
                    "title": "列表标题",
                    "summary": "列表摘要",
                    "entries": ["选项A", "选项B", "选项C"],
                    "entry_values": ["A", "B", "C"],
                    "default_value": "A"
                }
            ]
        },
        {
            "title": "配置组标题2",
            "config": "config2.json",
            "preferences": [
                {
                    "type": "button",
                    "title": "按钮标题",
                    "summary": "按钮摘要",
                    "arrow_type": "none",
                    "on_click": "com.example.Plugin.onClick"
                },
                {
                    "key": "key_name",
                    "type": "seekbar",
                    "title": "滑动条标题",
                    "default_value": 10.0,
                    "min": 1.0,
                    "max": 100.0
                },
                {
                    "key": "key_name",
                    "type": "edittext",
                    "title": "文本输入标题",
                    "summary": "文本输入摘要",
                    "default_value": "Hello World"
                }
            ]
        }
    ]
}
```

## 监听配置更改
插件开发者应该调用 `ConfigManager::addConfigChangeListener` 方法来监听配置更改事件，以获取用户在配置界面所做的更改，并做出响应

## 注意事项
- 在调用 `Manager::createConfigManager(pluginId: String)` 时传参的 pluginId 应该与插件清单中的 Plugin-Id 一致
- 确保配置项的 key 值与实际配置文件中的键名一致
- 按钮类型的配置项不需要 key 属性
- 方法调用使用反射实现，确保指定的方法是静态的、无参数且可访问
- 配置更改后会自动保存到对应的配置文件中
