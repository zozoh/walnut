命令简介
======= 

`likeit` 管理对一个目标的点赞功能

用法
=======

```bash
likeit 
  [Action]            # 执行操作， yes|no|all|count|is
  [TargetId]          # 被赞目标 ID
  [UID ...]           # 用户 ID 可多个, all|count 不需要这个参数
  [-ajax]             # 输出为 AJAX 格式
  [-json]             # 输出为 JSON 格式
  [-cqn]              # 对 JSON/AJAX 格式的格式化
  [-conf {Name}]      # 指定配置文件名，默认用 `_like`
                      # 名称会自动加上 `.json` 后缀
                      # 配置文件存放在 ~/.domain/like/ 目录下
  [-i 1]              # 对于 all 命令按行输出， 起始行号，默认为 1
  [-out '%d) %s']     # 对于 all 命令按行输出， 输出模板
```

示例
=======

```bash
# 指定 wendal/zozoh 赞赏 cake
demo:$> likeit yes cake wendal zozoh
2

# 获取 cake 的赞赏数量
demo:$> likeit count cake -json -c
2

# 获取 cake 的赞赏人列表
demo:$> likeit all cake -json -c
["zozoh","wendal"]

# 指定 zozoh 对 cake 取消赞赏
demo:$> likeit no cake zozoh
1

# 获取 cake 的赞赏数量
demo:$> likeit count cake -ajax -c
{ok:true, data: 1}

# 判断 wendal 是否赞赏 cake
demo:$> likeit is cake wendal
true

# 判断 zozoh 是否赞赏 cake
demo:$> likeit is cake zozoh -json -cqn
false

```