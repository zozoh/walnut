# 简介

`app i18n` 命令用来辅助用户快速获得自己自定义的 i18n 设置。

它会依次在 `$UI_PATH`（默认 `/etc/ui`）下的每个 UI 主目录里查找 `i18n/<语言>.js` 文件，读取其中的键值对并输出 JSON。

# 用法

```bash
app i18n [lang] [-merge] [-cqn]
            # lang    # 指定语言，默认 zh-cn
            # -merge  # 融合所有 UI 主目录下的语言设定；默认只取第一个找到的文件
            # -c      # 按 JSON 输出时，紧凑显示
            # -q      # 按 JSON 输出时，键名用双引号包裹
            # -n      # 按 JSON 输出时，null 值的键不忽略
```

# 示例

```bash
# 默认获取 zh-cn
app i18n

# 获取指定的语言
app i18n en-us

# 合并所有 UI 目录下的语言设定，后面的目录不覆盖前面的键
app i18n -merge
```
