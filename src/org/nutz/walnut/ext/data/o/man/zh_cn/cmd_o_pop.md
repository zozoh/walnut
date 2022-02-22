# 过滤器简介

`@pop` 从上下文中所有对象弹出数据

# 用法

```bash
o @pop
  [-null]           # 如果最后删成空数组，是否会置 null
  [KEY:VAL, ...]    # 多个值, 指明如何删除，格式为 KEY[:VAL]
                    # KEY 表示处理的键
                    # VAL 值的格式如下：
                    #  - 3      : 从后面弹出最多三个
                    #  - -1     : 从开始处弹出最多一个
                    #  - i3     :  0 base下标，即第四个
                    #  - i-1    : 最后一个
                    #  - i-2    : 倒数第二个
                    #  - =xyz   : 弹出内容为 'xyz' 的项目
                    #  - !=xyz  : 弹出内容不为 'xyz' 的项目
                    #  - [a,b]  : 弹出半角逗号分隔的列表里的值
                    #  - ![a,b] : 弹出不在半角逗号分隔的列表里的值
                    #  - ^a.*   : 弹出被正则表达式匹配的项目
                    #  - !^a.*  : 弹出没有被正则表达式匹配的项目
                    #  - <nil>  : 删除全部空数据项目
                    #  - <all>  : 清空全部数据
                    # 默认的，VAL 为 i-1
```

# 示例

```bash
#--------------------------------------------------------
# 假设有一个对象，names=["A", "B", "C", "D", "E"]
#--------------------------------------------------------
# 从后面弹出两个
$demo:> o abc @pop names:2 @json -cqn "^(id|names)$"
{"id":"0cnhdbir8sin3q88himugvf40h","names":["A", "B", "C"]}

# 从开始处弹出一个
$demo:> o abc @pop names:-1 @json -cqn "^(id|names)$"
{"id":"0cnhdbir8sin3q88himugvf40h","names":["B", "C", "D", "E"]}

# 弹出倒数第二个
$demo:> o abc @pop names:i-2 @json -cqn "^(id|names)$"
{"id":"0cnhdbir8sin3q88himugvf40h","names":["A", "B", "C", "E"]}
```

