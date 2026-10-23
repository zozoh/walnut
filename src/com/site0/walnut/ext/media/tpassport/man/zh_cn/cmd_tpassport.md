# 命令简介

`tpassport` 文字图章生成器：按配置把文字渲染成图片

# 用法

```bash
tpassport @create [配置路径] [-data JSON或文件] [-dst 输出图片]
```

支持的处理器：

```bash
@create    # 按配置渲染文字图章
```

# 示例

```bash
# 配置从管道读，渲染结果存为 JPEG
cat ~/tp/conf.json | tpassport @create -dst ~/tp/out.jpg

# 配置为路径，数据替换配置里的文字项
tpassport @create ~/tp/conf.json -data '{title:"合格"}' -dst ~/tp/out.jpg
```
