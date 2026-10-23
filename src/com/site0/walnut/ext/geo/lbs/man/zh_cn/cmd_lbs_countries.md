# 过滤器简介

`@countries` 获取全部国家的编码和名称等信息。每个国家信息的格式：

```js
{
  "key": "CN",
  "name": {
    "en_us": "China",
    "zh_cn": "中国"
  }
}
```

# 用法

```bash
lbs @countries [国家编码...] [-lang 语言] [-map 模式] [-ajax] [-cqn]
       # 国家编码    # 指定几个国家，如 CN US；不给则输出全部
  -lang      # 显示国家的语言，默认 zh_cn
  -map       # 用名值对形式输出，键为国家编码；
             # name 值为国家显示名，obj 值为整个 country 对象
  -ajax      # 按 AJAX 格式输出
  -cqn       # JSON 输出的格式化开关
```

# 示例

```bash
# 得到国家列表
lbs @countries -lang en_us -cq
# [{"key":"US","name":"United States of America (USA)"}, ...]

# 得到指定几个国家的列表
lbs @countries CN US -lang en_us -cq

# 得到国家的映射表
lbs @countries -lang en_us -map name -ajax -cq
# {"ok":true,"data":{"CN":"China"... }}
```
