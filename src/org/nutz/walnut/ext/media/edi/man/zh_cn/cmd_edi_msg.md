# 过滤器简介

`@msg` 将上下文报文对象，按照某种方式解析为消息对象

# 用法

```bash
edi @msg 
    [IC]      # 解析报文对象的类型
              #  - IC : 控制报文返回
              #  - CLREGR : 注册接口返回
              #  - AUTO : 根据报文的内容自行决定【默认】
    [-cqn]    # 输出的 JSON 格式化
    [-l]      # 即使只有一条报文消息也要输出数组，否则会拆包
```

# 示例

```bash
edi @load ~/demo.edi.txt @parse @msg IC -cqn
```
