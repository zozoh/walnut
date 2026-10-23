# 过滤器简介

`@create` 按配置把文字渲染成图片（文字图章）。

配置是一个 JSON 文件（或从管道读入），描述图章的绘制项（`items`）；`-data` 提供的数据会按绘制项的 `name` 替换其文字内容。

# 用法

```bash
tpassport @create [配置路径] [-data JSON或文件] [-dst 输出图片]
  -data    # JSON 字符串或 JSON 文件路径，替换配置里绘制项的文字内容
  -dst     # 输出图片路径（JPEG）；不给定则不落盘
```

# 示例

```bash
# 配置从管道读，渲染结果存为 JPEG
cat ~/tp/conf.json | tpassport @create -dst ~/tp/out.jpg

# 配置为路径，数据替换配置里的文字项
tpassport @create ~/tp/conf.json -data '{title:"合格"}' -dst ~/tp/out.jpg
```
