# 过滤器简介

`@creation` 用来获取某个 `DIR` 对象下可以被创建子对象的类型列表。

> [文档](https://github.com/zozoh/titanium/blob/master/doc/en-us/walnut/creation.md)

# 用法

```bash
ti @creation [对象路径] [-lang 语言] [-cqn]
  # 对象路径    # 根据对象自动寻找 creation 定义
  -lang        # 输入语言，默认 zh-cn
  -cqn         # JSON 输出的格式化开关
```

# 示例

```bash
# 根据对象得到可以创建的子对象类型列表
ti @creation id:xxx
```
