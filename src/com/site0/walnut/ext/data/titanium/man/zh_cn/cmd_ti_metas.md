# 过滤器简介

`@metas` 用来获取某个对象的元数据定义信息。

> [文档](https://github.com/zozoh/titanium/blob/master/doc/zh-cn/walnut/metas.md)

对象路径与 `-name` 必须要给定一个，否则本命令不知道要输出什么。

# 用法

```bash
ti @metas [对象路径] [-name 定义名] [-m 文件名] [-cqn]
  # 对象路径    # 根据对象自动寻找元数据定义
  -name        # 直接指定定义名称，这个更优先
  -m           # 映射文件名，默认 metas.json
  -cqn         # JSON 输出的格式化开关
```

# 示例

```bash
# 根据对象自动选择一个定义
ti @metas id:xxx

# 直接输出某个定义
ti @metas -name "my-meta"
```
