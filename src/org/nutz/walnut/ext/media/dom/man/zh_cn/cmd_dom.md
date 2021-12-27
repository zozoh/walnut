# 命令简介

`html` 是一个用来处理HTML文档的过滤型命令。
它的上下文维护了一棵 DOM 树

```bash
/Context
|-- document      # 文档树
|   |-- $head     # 文档头
|   |-- $body     # 文档内容
|-- current       # 当前操作节点
```

# 用法

```
dom [options] [[@filter filter-args...]...]
```

它支持的 options 有:

```bash
[-body]      # 文档内容只有 body 内的内容
```

它支持的过滤器有：

```bash
@as          # 作为什么格式输出
@docx        # 将上下文的DOM输出为docx
@find        # 寻找符合类选择器的元素
@format      # 格式化 DOM 节点
@html        # 加载一个 HTML 文档
@mutate      # 操作 DOM 节点，根据配置修改节点
@refimg      # 寻找指定的图片节点，修改映射的 ID
@reset       # 重置当前操作节点
@json        # 将上下文内容按照 JSON 输出
```
