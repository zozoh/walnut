命令简介
======= 

`lbs countries` 获取全部国家的编码和名称等信息。
程序缓存了全部国家的信息，每个国家信息的格式如下：

```js
{
  "key": "CN",
  "name": {
    "en_us": "China",
    "zh_cn": "中国"
  }
}
```
    
用法
=======

```bash
lbs countries
    [-lang zh_cn]  # 显示国家的语言，默认 zh_cn
    [-map name]    # 用名值对的形式输出，键为国家编码，值为国家的显示名
                   # 如果 -map obj 则，值为整个 country 对象
                   # 如果不是 name 或者 obj 则无视
    [-ajax]        # 「选」按照 AJAX 返回输出，默认按照 JSON 方式
    [-cqn]         # 「选」JSON 输出的格式化方式
```

示例
=======

```bash
# 得到一个国家的列表
demo@~$ lbs countries -lang en_us -cq
[{"key":"US","name":"United States of America (USA)"}, ...]

# 得到指定几个国家的列表
demo@~$ lbs countries CN US -lang en_us -cq
[{"key":"CN","name":"China"}, {"key":"US","name":"United States of America (USA)"}]

# 得到一个国家的映射表
demo@~$ lbs countries -lang en_us -map name -ajax -cq
{"ok":true,"data":{"CN":"China"... }}

# 得到某几个国家的映射表
demo@~$ lbs countries CN US -lang en_us -map name -ajax -cq
{"ok":true,"data":{"US":"United States of America (USA)","CN":"China"}}
```