# 命令简介

`favor` 管理对一个目标的收藏功能，数据存放在 Redis。

# 用法

```bash
favor 动作 用户ID [目标ID...] [-conf 名] [-obj id] [-limit N] [-skip N] [-rever] [-quiet] [-ajax] [-json] [-pager] [-cqn] [-i N] [-ms] [-dv 值] [-df 格式] [-out 模板]
       # 动作     # yes 收藏 | no 取消收藏 | all 收藏列表 | count 数量 | when 收藏时间
       # 用户ID
       # 目标ID   # 被收藏主体 ID，all|count 不需要
  -conf      # 配置文件名（自动加 .json 后缀），默认 _favor，存放在 ~/.domain/favor/ 下
  -obj       # all(ajax|json)：target 作为对象 ID 读取其元数据
  -limit     # all：最大获取数量，0 表示不限制
  -skip      # all：跳过多少条记录，默认 0
  -rever     # all：反序
  -quiet     # 静默模式，什么都不输出
  -ajax      # 输出 AJAX 格式
  -json      # 输出 JSON 格式
  -pager     # AJAX|JSON 输出时带上翻页信息
  -cqn       # JSON/AJAX 的格式化开关
  -i         # all 按行输出的起始行号，默认 1
  -ms        # when 命令输出保持毫秒数形式
  -dv        # when 命令无记录时的返回值，nil 表示 null
  -df        # when 命令输出的时间格式，默认 yyyy-MM-dd HH:mm:ss
  -out       # all 按行输出的模板，默认 '%d) %s + %s'
```

# 示例

```bash
# zozoh 收藏了 cake banana
favor yes zozoh cake banana
# 2

# 获取 zozoh 的收藏数量
favor count zozoh -json -c
# 2

# 获取 zozoh 的收藏列表
favor all zozoh -json -c
# [{target:"cake",time:159..231},{target:"banana",time:159..231}]

# zozoh 取消收藏 cake
favor no zozoh cake

# zozoh 什么时候收藏的 banana
favor when zozoh banana
# 1591201918164

favor when zozoh cake -json -cqn
# 0
```
