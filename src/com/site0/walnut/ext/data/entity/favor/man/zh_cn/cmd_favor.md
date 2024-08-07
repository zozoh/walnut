命令简介
======= 

`favor` 管理对一个目标的收藏功能

用法
=======

```bash
favor 
  [Action]              # 执行操作， yes|no|all|count|when
  [UID]                 # 用户ID
  [TargetId ...]        # 被收藏主体ID, all|count 不需要这个参数
  [-conf {Name}]        # 指定配置文件名，默认用 `_favor`
                        # 名称会自动加上 `.json` 后缀
                        # 配置文件存放在 ~/.domain/favor/ 目录下
  [-obj id]             # all(ajax|json): target作为对象ID读取其元数据
  [-limit 0]            # all: 设定最大获取数量，0 表示不限制
  [-skip 0]             # all: 跳过多少条记录，默认 0
  [-rever]              # all: 反序
  [-quiet]              # 静默模式，什么都不输出
  [-ajax]               # 输出为 AJAX 格式
  [-json]               # 输出为 JSON 格式
  [-pager]              # 当 AJAX|JSON 输出时，是否带上翻页信息
  [-cqn]                # 对 JSON/AJAX 格式的格式化
  [-i 1]                # 对于 all 命令按行输出， 起始行号，默认为 1
  [-ms]                 # 对于when命令输出的时间格式，保持毫秒数的形式
  [-dv nil]             # 对于when命令，如果没有记录，返回什么
                        # 如果是 nil 表示 null
                        # 如果没有声明，则根据 -ms 和 -df 开关看输出结果
  [-df yyyyMMdd]        # 对于when命令输出的时间格式，默认 `yyyy-MM-dd HH:mm:ss`
  [-out '%d) %s + %s']  # 对于 all 命令按行输出， 输出模板
```

示例
=======

```bash
# zozoh 收藏了 cake banana
demo:$> favor yes zozoh cake banana
2

# 获取 zozoh 的收藏数量
demo:$> favor count zozoh -json -c
2

# 获取 cake 的收藏列表
demo:$> favor all zozoh -json -c
[{target:"cake",time:159..231},{target:"banana",time:159..231}]

# 指定 zozoh 对 cake 取消收藏
demo:$> favor no zozoh cake
1

# 获取 zozoh 的收藏数量
demo:$> favor count cake -ajax -c
{ok:true, data: 1}

# 判断 zozoh 什么时候收藏的 banana
demo:$> favor when zozoh banana
1591201918164

# 判断 zozoh 什么时候收藏的 cake
demo:$> favor when zozoh cake -json -cqn
0

```