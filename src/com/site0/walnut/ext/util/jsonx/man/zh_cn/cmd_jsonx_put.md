# 过滤器简介

`@put` 向上下文增加特殊的键值

# 用法

```bash
@put
    [key]         # 键名
    [value]       # 值或者是 JSON 对象|数组
                  # 如果是 “:stdin” 则从标志输入读取值
    -dft          # 将值作为默认值
    -path         # 键名用 .  作为路径
    -raw          # 键值不要自动转换对象，直接用字符串
    -trim         # 处理前，先 trim 掉值的前后空白
```

# 示例

```bash
# 加入一个字符串
echo '{}' | jsonx @put title 'Good Day'
{title:"Good Day"}

# 加入一个长字符串
echo 'hello world' | jsonx {} @put msg :stdin
{msg:'hello world'}

# 加入一个对象
echo '{}' | jsonx @put pos '{x:100,y:99}'
{pos:{x:100,y:99}}

# 仅仅作为默认值添加
echo '{pos:{x:80}}' | jsonx @put -dft 'pos' '{x:100}'
{pos: {x:80}}

# 将当前对象记入一个新 map
echo '{x:100}' | jsonx @put 'pos'
{pos: {x:100}}

# 设置值到深层对象
echo '{x:100}' | jsonx @put -path 'pet.name' 'xiaobai'
{x:100, pet: {name:"xiaobai}}
```

