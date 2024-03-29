# 过滤器简介

`@json` 将选择的节点作为 JSON 输出

# 用法

```bash
dom @json 
  [elTag=elVal]    # 可以多个，表示节点的子节点也加入属性
                   # elTag 表示指定的属性，可以是 ^ 开头正则表达式
                   # 或者 *，表示通配所有子节点
                   # elVal 表示采用哪个属性作为值，!TEXT 表示采用文本节点
  [-name ^w:(.+)$] # 指定了一个名称格式化的方法，根据正则表达式，默认获取第一组
                   # 作为名称，主要用来通用处理属性前缀
                   # 对于属性和子元素都会生效
                   # 如果不匹配，则维持原来的属性名
  [-map key]       # 根据对象某个键，集合成 Map
                   # 否则，将作为 List 输出
  [-cqn]           # JSON 模式下的输出格式
```

# 示例

```bash
# 仅仅输出 HTML 文本内容
dom @html a.html @as text
```

