# 过滤器简介

`@ancestors` 将上下文中的对象的所有祖先对象，加入上下文

# 用法

```bash
o @ancestors
  [parents]            # 将祖先节点设置到对象的哪个键中存储
                       # 这个与 keep 选项互斥且具有更高的优先级。
                       # 也就是说，如果声明本选项，那么 -keep 就没用了 
                       # 如果本选项声明为 "~ignore~"，则表示本操作将被跳过
                       # 同时，如果值为 ~ignore~，相当于 null，即未设定
  [-until /path/to]    # 指定一个截止的祖先节点路径，默认 `~`
  [-um {...}]          # 【选】指定一个截止的祖先节点条件
  [-self]              # 输出也包括自己
  [-notop]             # 输出不包括第一个祖先节点
  [-keep]              # 并不清空上下文
```

# 示例

```bash
# 列出指定对象的祖先节点（包括自己）
o @fetch ~/abc/x/y/z.txt @ancestors -unti ~/abc -self 
```

