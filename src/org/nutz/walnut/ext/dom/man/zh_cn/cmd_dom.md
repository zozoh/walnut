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
@html        # 加载一个 HTML 文档
@format      # 格式化 DOM 节点
@heading     # 寻找到 HTML 第一个符合要求的标题
@outline     # 输出 HTML 的大纲级别
@reset       # 重置当前操作节点
@as          # 作为什么格式输出
```
