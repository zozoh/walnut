命令简介
======= 

`buy` 管理一个购物车集合，将对象放入购物车或者删除等操作

用法
=======

```bash
buy
  [Action]            # 执行操作， it|rm|clean|all|count|sum|get
  [UID]               # 用户 ID，all|sum 不需要这个参数
  [TargetId]          # 商品 ID
  [N(1)]              # 数量，all|sum 不需要这个参数, get 则表示默认返回数量
  [-reset]            # 表示 N 是一个绝对数量，所以如果 N<=0 则表示删除
  [-rever]            # all: 反序
  [-obj]              # all: 同时获取对象内容（N+1）查询
  [-quiet]            # 静默模式，什么都不输出
  [-ajax]             # 输出为 AJAX 格式
  [-json]             # 输出为 JSON 格式
  [-cqn]              # 对 JSON/AJAX 格式的格式化
  [-conf {Name}]      # 指定配置文件名，默认用 `_buy`
                      # 名称会自动加上 `.json` 后缀
                      # 配置文件存放在 ~/.domain/buy/ 目录下
  [-i 1]              # 对于 all 命令按行输出， 起始行号，默认为 1
  [-out '%d) %d <- %s']  # 对于 all 命令按行输出， 输出模板
```

示例
=======

```bash
# zozoh 购买了一个 cake
demo:$> buy it zozoh cake
1

# zozoh 又购买了两个 cake
demo:$> buy it zozoh cake 2
1

# 获取 zozoh 购买的全部东西
demo:$> buy all zozoh -json -cqn
[{name:"cake",count:3}]

# 获取 zozoh 购买了多少 cake
demo:$> buy get zozoh cake
3

# zozoh 放弃了一个 cake
demo:$> buy it zozoh cake -1
117

# zozoh 放弃了所有的 cake
demo:$> buy it zozoh cake 0 -reset
117

# zozoh 清空了购物车
demo:$> buy clean zozoh
59

# 获取 zozoh 购物车商品的品类总数
demo:$> buy count zozoh
18

# 获取 zozoh 购物车商品的个数总数
demo:$> buy sum zozoh
-1

```