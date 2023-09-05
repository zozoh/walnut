# 过滤器简介

`@item` 从上下文列表中获取一个元素，并设置到上下文

# 用法

```bash
@item [Index]   # 元素下标， -1 标识最后一个元素
```

# 示例

```bash
# 获取第一个
echo '["A","B","C"]' | jsonx @item 0
> "A"

# 获取最后一个
echo '["A","B","C"]' | jsonx @item -1
> "C"
```

