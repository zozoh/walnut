# 过滤器简介

`@merge` 向上下文融合一个或者多个指定的对象
 
# 用法

```bash
@akeys [OBJ ..]  # 多个对象的 JSON 字符串
```

# 示例

```bash
echo '{}' | jsonx @merge '{x:100}' '{y:99}'
```

