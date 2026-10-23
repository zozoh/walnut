# 过滤器简介

`@views` 用来获取某个对象的视图映射信息。

> [视图文档](https://github.com/zozoh/titanium/blob/master/doc/en-us/walnut/view.md)

对象路径与 `-name` 必须要给定一个，否则本命令不知道要输出什么。

# 用法

```bash
ti @views [对象路径] [-name 视图名] [-m 文件名] [-cqn]
  # 对象路径    # 根据对象自动寻找视图
  -name        # 直接指定视图名称，这个更优先
  -m           # 映射文件名，默认 mapping.json
  -cqn         # JSON 输出的格式化开关
```

# 示例

```bash
# 根据对象自动选择一个视图
ti @views id:xxx

# 直接输出某个视图
ti @views -name "text-editor"
```
