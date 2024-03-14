# 过滤器简介

`@set` 向上下文设置自定义JSON对象。
与 `@merge` 不同的是，它并不会做深层递归融合。

# 用法

```bash
@set [JSON ...]   # 多个 JSON 对象字符串
```

# 示例

```bash
# 强制覆盖
echo '{}' | jsonx @set '{x:100}' 'y:99'
{x:100, y:99}

```

