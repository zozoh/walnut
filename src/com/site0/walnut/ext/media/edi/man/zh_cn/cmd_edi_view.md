# 过滤器简介

`@view` 输出上下文信息

# 用法

```bash
edi @view  
    [-as json]  # 指定输出形式
                #  - json 将报文对象作为 JSON 输出
                #  - tree 将报文对象输出为树形结构
                #  - text 将报文对象输出文本
                #  - vars 输出加载关联上下文变量部分
                #  - msg  直接输出原始的报文消息
                #  - raw  加载了什么，就是输出什么
                # 默认按照 json 输出
    [-cqn]      # 当输出是 JSON 模式时
```

# 示例

```bash
edi @load ~/demo.edi.txt @parse @view -as json -cqn
```
