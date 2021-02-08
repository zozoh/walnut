# 过滤器简介

`@as` 作为什么格式输出

# 用法

```bash
dom @as 
  [html|text|json]   # 输出格式
  [-cqn]             # JSON 模式下的输出格式
  [-doc]             # 强制输出整个文档
  [-selected]        # 强制输出选择节点
```

# 示例

```bash
# 仅仅输出 HTML 文本内容
dom @html a.html @as text
```

