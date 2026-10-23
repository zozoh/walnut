# 命令简介

`buy` 管理一个购物车集合：将对象放入购物车、删除、统计等操作，数据存放在 Redis。

# 用法

```bash
buy 动作 用户ID [商品ID] [数量] [-reset] [-rever] [-obj] [-quiet] [-ajax] [-json] [-cqn] [-conf 名] [-i N] [-out 模板]
       # 动作     # it 放入 | rm 删除 | clean 清空 | all 全部 | count 种数 | sum 总数 | get 查询
       # 用户ID   # all|sum|count 外必填
       # 商品ID   # all|sum|count 外必填
       # 数量     # 默认 1；-reset 时 N<=0 表示删除
  -reset    # N 是一个绝对数量
  -rever    # all：反序输出
  -obj      # 同时获取对象内容（N+1 次查询）
  -quiet    # 静默模式，什么都不输出
  -ajax     # 输出 AJAX 格式
  -json     # 输出 JSON 格式
  -cqn      # JSON/AJAX 的格式化开关
  -conf     # 配置文件名（自动加 .json 后缀），默认 _buy，存放在 ~/.domain/buy/ 下
  -i        # all 按行输出时的起始行号，默认 1
  -out      # all 按行输出的模板，默认 '%d) %d <- %s'
```

# 示例

```bash
# zozoh 购买了一个 cake
buy it zozoh cake
# 1

# zozoh 又购买了两个 cake
buy it zozoh cake 2

# 获取 zozoh 购买的全部东西
buy all zozoh -json -cqn
# [{name:"cake",count:3}]

# 获取 zozoh 购买了多少 cake
buy get zozoh cake
# 3

# zozoh 放弃了一个 cake
buy it zozoh cake -1

# zozoh 放弃了所有的 cake
buy it zozoh cake 0 -reset
```
