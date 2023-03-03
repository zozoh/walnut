# 过滤器简介

`@assign` 向上下文覆盖指定的键

# 用法

```bash
@put [JSON ...]   # 多个 JSON 对象字符串
```

# 示例

```bash
# 强制覆盖
echo '{x:80}' | jsonx @assign '{x:100}'
{x:100}

# 仅仅作为默认值添加
echo '{x:80}' | jsonx @assign -dft '{x:100}'
{x:80}
```

