# 过滤器简介

`@view` 输出上下文信息

# 用法

```bash
edi @view  
    [-as json]  # 指定输出形式
                #  - json 将报文对象作为 JSON 输出
                #  - tree 按照树形结构输出报文对象
                #  - text 直接输出上下文报文文本
                # 默认按照 json 输出
    [-cqn]      # 当输出是 JSON 模式时
```

# 示例

```bash
edi @load ~/demo.edi.txt @parse @view -as json -cqn
```
