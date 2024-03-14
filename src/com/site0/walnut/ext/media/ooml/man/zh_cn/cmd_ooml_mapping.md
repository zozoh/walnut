# 过滤器简介

`@mapping` 为上下文设置Bean映射关系

# 用法

```bash
ooml @mapping
    [{..}]               # 映射关系（JSON）
    [-f /path/to]        # 指定映射关系存放的 JSON 文件
                         # 如果没有声明映射关系，会自动从标准输入读取
    [-reset]             # 重置上下文映射关系
    [-only]              # 只有映射表里面的字段才会加入 Bean 中
    [-defaults {..}]     # 设置映射表的默认值
    [-override {..}]     # 设置映射表的强制覆盖值
    [-keys AutoMatch]    # 只有匹配的键才会被保留(对应 fld.key)
    [-names AutoMatch]   # 只有匹配的输出字段名才会被保留(对应 fld.name)
```

# 示例

```bash
# 读取数据映射关系到上下文
ooml ~/abc.xlsx @mapping -f mapping.json
```

