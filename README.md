# SensitiveWordFilter 插件
## 当前版本未完成书本检测（其实是不知道多版本兼容怎么写了），如有需要可自行增加，函数已预留好，别忘记增加Pull Requests哦
### 理论版本支持 Spigot1.8-1.21 为增加版本兼容，多数代码已完成替代方案
## 简介
这是一款用于 Minecraft 服务器的敏感词过滤插件。它可以对玩家的聊天内容、告示牌文字以及铁砧命名等进行敏感词过滤，从而净化服务器发言。

## 功能特性
1. **敏感词过滤**：对玩家的聊天消息、告示牌文字、书本内容和铁砧命名中的敏感词进行过滤。
2. **专有名词保护**：支持专有名词词库，确保专有名词不会被误过滤。
3. **过滤模式设置**：提供两种过滤模式，分别是宽松模式（`permissive`）和严格模式（`enforcing`）。
    - **宽松模式**：类似于腾讯游戏的过滤模式，直接将敏感词替换为 `*`。
    - **严格模式**：类似于网易三星堆 `***` 不解释。

## 安装与配置

### 1. 安装
- 将插件放置在服务器的 `plugins` 目录下。
- 重启服务器，插件将自动自动释放词库文件。

### 2. 配置文件
插件启动后，会在 `plugins/SensitiveWordFilter` 目录下生成以下文件：
- **`sensitive_words.txt`**：用于存储敏感词，每行一个敏感词。
- **`proper_nouns.txt`**：用于存储专有名词，每行一个专有名词。
- **`config.yml`**：用于配置过滤模式。

# 目前源代码中已内置腾讯开源敏感词库，专有名词词库为AI生成，有需要可自行修改
