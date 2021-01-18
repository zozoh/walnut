# 过滤器简介

`@bish` 将上下文中的对象列表按照表格输入

# 用法

```bash
o @tab 'Tmplate content' ...
  [-bish]       # 表格的格式化方式
  [-ibase 0]    # 编号起始值，默认 0
```

# 示例

```bash
# 输出所有节点的 ID
o ~/abc @query '{tp:"txt"}' @tab -bish id nm title
```

