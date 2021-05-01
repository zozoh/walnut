命令简介
======= 

`lbs freight` 运费计算命令

> 由于各国运费计算方式不一，现在只考虑中国区情况
    
用法
=======

```bash
lbs freight
  [$ADDR_FROM]      # 发货地，一个地址编码，无论12位还是6位，只有前六位有效
  [$ADDR_TO]        # 收货地，一个地址编码，无论12位还是6位，只有前六位有效
                    # 如果不填，表示 00 即，发往全国
  [-weight 10.5]    # 物品总重量（公斤）
  [-conf xxx.json]  # 运费规则表，默认在 ~/.domain/freight_sheet.json
  [-country CN]     # 国家编码，默认 CN
  [-ajax]      # 「选」按照 AJAX 返回输出，默认按照 JSON 方式
  [-cqn]       # 「选」JSON 输出的格式化方式
```

```bash
# 计算北京发往上海运费
demo@~$ lbs freight 11 31 -weight 22.5
{
  # 根据输入分析的重量
  "weight" : {
    "first"      : 1,       # 首重（公斤）
    "additional" : 21.5,    # 续重（公斤）
  },
  # 找到的匹配规则
  "rule" : {
    "title"        : "北京至全国",  # 标题（助记）
    "ship_code"    : "110000",     # 发货地（六位地址编码）
    "target_code"  : "000000",     # 目的地址（六位地址编码）
    "first"        : 10.5,         # 首重价格（元）
    "additional"   : 0.8           # 续重价格（元）
  },
  "first"      : 10.5,       # 首重价格（元）
  "additional" : 32,         # 续重总价格（元）
  "total"      : 42.5        # 总运费
}
```


