# 过滤器简介

`@config` 用来获取某个会话自定义的 Ti.Config 配置信息。

默认它会读取 `~/.ti/config.json` 文件，如果不存在则返回 `{}`。

# 用法

```bash
ti @config [-name 文件名] [-cqn]
  -name    # 配置文件名，默认 config.json
  -cqn     # JSON 输出的格式化开关
```

# 示例

```bash
# 获取会话配置
ti @config

# 指定其他配置文件
ti @config -name my-config.json
```
