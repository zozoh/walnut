# 过滤器简介

`@freight` 运费计算命令。

由于各国运费计算方式不一，现在只考虑中国区情况。

# 用法

```bash
lbs @freight 发货地 [收货地] [-weight 公斤] [-conf 规则表] [-country 编码] [-ajax] [-cqn]
       # 发货地    # 地址编码，无论 12 位还是 6 位，只有前六位有效
       # 收货地    # 地址编码；不填表示 00 即发往全国
  -weight    # 物品总重量（公斤）
  -conf      # 运费规则表，默认 ~/.domain/freight_sheet.json
  -country   # 国家编码，默认 CN
  -ajax      # 按 AJAX 格式输出，默认按 JSON 方式
  -cqn       # JSON 输出的格式化开关
```

输出：

```js
{
  "weight" : { "first": 1, "additional": 21.5 },     // 首重/续重（公斤）
  "rule"   : {                                        // 匹配到的规则
    "title": "北京至全国",
    "ship_code": "110000", "target_code": "000000",
    "first": 10.5, "additional": 0.8
  },
  "first": 10.5, "additional": 32, "total": 42.5     // 首重/续重/总运费（元）
}
```

# 示例

```bash
# 计算北京发往上海 22.5 公斤的运费
lbs @freight 11 31 -weight 22.5

# 发往全国
lbs @freight 11 -weight 1.5
```
