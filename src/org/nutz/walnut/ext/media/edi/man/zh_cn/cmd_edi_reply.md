# 过滤器简介

`@msg` 将上下文报文对象，按照某种方式解析为消息对象

# 用法

```bash
edi @msg 
    [IC]      #【必】解析报文对象的类型
              #  - IC : 控制报文返回
              #  - CLREGR : 注册接口返回
    [-cqn]    # 输出的 JSON 格式化
```

# 示例

```bash
edi @load ~/demo.edi.txt @parse @msg IC -cqn
```
