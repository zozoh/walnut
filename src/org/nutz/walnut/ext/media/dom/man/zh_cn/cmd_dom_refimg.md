# 过滤器简介

`@refimg` 寻找指定的图片节点，修改映射的 ID

主要是应对Copy文档，同时也需要 Copy 所有引用图片的场景

# 用法

```bash
dom @refimg
  [selector1, selector2]  # 选择器，默认的选择器是 img.wn-media
  [-from path/to/html]    # 原始的 HTML 文件路径，以便获取相对路径
  [-to path/do/html]      # 新的 HTML 路径，以便从相对路径获取新图片对象
```