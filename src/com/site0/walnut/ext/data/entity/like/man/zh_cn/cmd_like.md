# 命令简介

`like` 管理对一个目标的点赞（喜欢）功能，数据存放在 Redis。

# 用法

```bash
like 动作 目标ID [用户ID...] [-conf 名] [-quiet] [-ajax] [-json] [-cqn] [-i N] [-out 模板]
       # 动作     # yes 点赞 | no 取消赞 | all 赞列表 | count 赞数 | is 是否赞过
       # 目标ID   # 被赞目标 ID
       # 用户ID   # 可多个；all/count 不需要
  -conf     # 配置文件名（自动加 .json 后缀），默认 _like
            # 配置文件存放在 ~/.domain/like/ 目录下
  -quiet    # 静默模式，什么都不输出
  -ajax     # 输出 AJAX 格式（{ok:true,data:...}）
  -json     # 输出 JSON 格式
  -cqn      # JSON/AJAX 的格式化开关：紧凑 / 键名加引号 / 不忽略 null
  -i        # all 按行输出时的起始行号，默认 1
  -out      # all 按行输出时的模板，默认 '%d) %s'
```

# 示例

```bash
# 指定 wendal/zozoh 点赞 cake
like yes cake wendal zozoh

# 获取 cake 的点赞数
like count cake -json -c

# 获取 cake 的点赞人列表
like all cake -json -c

# zozoh 取消点赞
like no cake zozoh

# 判断 wendal 是否点赞过 cake
like is cake wendal
```
