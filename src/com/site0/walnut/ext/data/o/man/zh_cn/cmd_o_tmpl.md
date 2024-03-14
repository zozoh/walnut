# 过滤器简介

`@tmpl` 将上下文中的对象按照一个模板字符串输出

# 用法

```bash
o @tmpl 'Tmplate content' ...
  [-sep \n]     # 指定分隔符，默认为 \n
```

# 示例

```bash
# 输出所有节点的 ID
o ~/abc @query '{tp:"txt"}' @tmpl '${id} : ${nm}' 
```

