命令简介
======= 

`mq send` 向系统消息队列加入一条消息

用法
=======

```bash
# 推入多个消息，消息输入可以有两种形式:
#  - JSON 形式，即前后由 {} 包裹的字符串
#  - FILE 形式，即由 '<-' 开头的字符串
#  - TEXT 形式, 其他情况，一律当作消息的文本形式
# 同时，本命令也支持从管道获取，即没有任何输入参数的情况下，
# 会读取管道内全部文本，根据文本内容判断是 JSON|TEXT|FILE 哪种形式
mq send [Message1] [Message2]
  [-t sys]   # 主题，默认为 sys
```

示例
=======

```bash
# 加入一条 JSON 形式的消息
demo:> mq send '{body:"echo xx > xx.txt"}'

# 加入一条 TEXT 形式的消息
demo:> mq send '@user=demo\n\necho xx > xx.txt'

# 加入两条对象形式的消息
demo:> mq send '<-id:xxx' '<-~/path/to/msg.txt'

# 通过管道添加消息
demo:> cat ~/msgs.txt | mq send
```