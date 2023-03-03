# 过滤器简介

`@put` 向上下文增加特殊的键值

# 用法

```bash
@put [JSON ...]   # 多个 JSON 对象字符串，或者普通字符串
```

# 示例

```bash
# 加入一个字符串
echo '{}' | jsonx @put title 'Good Day'
{title:"Good Day"}

# 加入一个对象
echo '{}' | jsonx @put pos '{x:100,y:99}'
{pos:{x:100,y:99}}

# 仅仅作为默认值添加
echo '{pos:{x:80}}' | jsonx @put -dft 'pos' '{x:100}'
{pos: {x:80}}

# 将当前对象记入一个新 map
echo '{x:100}' | jsonx @put 'pos'
{pos: {x:100}}
```

