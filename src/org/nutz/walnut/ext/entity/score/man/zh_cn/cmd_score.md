命令简介
======= 

`score` 管理对一个目标的打分功能

用法
=======

```bash
score
  [Action]            # 执行操作， it|cancel|all|count|sum|avg|resum|get
  [TargetId]          # 被赞目标 ID
  [UID]               # 用户 ID，all|sum 不需要这个参数
  [N(80)]             # 分值，all|sum 不需要这个参数
  [-limit 100]        # all: 获取某个限定的人数打分详情，all 需要这个参数
  [-skip 0]           # all: 跳过多少条记录，默认 0
  [-rever]            # all: 反序
  [-quiet]            # 静默模式，什么都不输出
  [-ajax]             # 输出为 AJAX 格式
  [-json]             # 输出为 JSON 格式
  [-cqn]              # 对 JSON/AJAX 格式的格式化
  [-conf {Name}]      # 指定配置文件名，默认用 `_like`
                      # 名称会自动加上 `.json` 后缀
                      # 配置文件存放在 ~/.domain/like/ 目录下
  [-i 1]              # 对于 all 命令按行输出， 起始行号，默认为 1
  [-out '%d) %d <- %s']  # 对于 all 命令按行输出， 输出模板
```

示例
=======

```bash
# cake 被 zozoh 打了 18 分
demo:$> score it cake zozoh 18
1

# cake 被 wendal 打了 99 分
demo:$> score it cake wendal 99
1

# 获取 cake 前 100 个打分人的打分详情
demo:$> score all cake -limit 100 -json -cqn
[{name:"zozoh",score:18},{name:"wendal",score:99}]

# 获取 cake 被打总分
demo:$> score sum cake
117

# 获取 cake 被打的平均分
demo:$> score avg cake
59

# 获取 cake 被 wendal 打的分数
demo:$> score get cake wendal
18

# 获取 cake 被 zozoh 打的分数
demo:$> score get cake zozoh -1
-1

# 重新计算 cake 的总分
demo:$> score recount cake
18

```