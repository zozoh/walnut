# 过滤器简介

`@ancestors` 将上下文中的对象的所有祖先对象，加入上下文

# 用法

```bash
o @ancestors 
  [-until /path/to]    # 指定一个截止的祖先节点，默认 ~
  [-self]              # 输出也包括自己
  [-notop]             # 输出不包括第一个祖先节点
  [-keep]              # 并不清空上下文
```

# 示例

```bash
# 列出指定对象的祖先节点（包括自己）
o @fetch ~/abc/x/y/z.txt @ancestors -unti ~/abc -self 
```

