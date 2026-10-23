# 命令简介

`score` 管理对一个目标的打分功能，数据存放在 Redis。

# 用法

```bash
score 动作 目标ID [用户ID] [分值] [-limit N] [-skip N] [-rever] [-quiet] [-ajax] [-json] [-cqn] [-conf 名] [-i N] [-out 模板]
       # 动作     # it 打分 | cancel 取消 | all 打分详情 | count 人数 | sum 总分 | avg 平均分 | resum 重算总分 | get 查询
       # 目标ID   # 被评目标 ID
       # 用户ID   # all|sum|avg 不需要
       # 分值     # 默认 80
  -limit     # all：限定人数的打分详情
  -skip      # all：跳过多少条记录，默认 0
  -rever     # all：反序
  -quiet     # 静默模式，什么都不输出
  -ajax      # 输出 AJAX 格式
  -json      # 输出 JSON 格式
  -cqn       # JSON/AJAX 的格式化开关
  -conf      # 配置文件名（自动加 .json 后缀），默认 _score，存放在 ~/.domain/score/ 下
  -i         # all 按行输出的起始行号，默认 1
  -out       # all 按行输出的模板，默认 '%d) %d <- %s'
```

# 示例

```bash
# cake 被 zozoh 打了 18 分
score it cake zozoh 18
# 1

# cake 被 wendal 打了 99 分
score it cake wendal 99

# 获取 cake 前 100 个打分人的打分详情
score all cake -limit 100 -json -cqn
# [{name:"zozoh",score:18},{name:"wendal",score:99}]

# 获取 cake 的总分 / 平均分
score sum cake
# 117
score avg cake
# 59

# 获取 cake 被 wendal 打的分数
score get cake wendal
# 18

# wendal 没打过分时返回 -1
score get cake wendal -1

# 重新计算 cake 的总分
score resum cake
```
