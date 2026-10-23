# 过滤器简介

`@send` 向系统消息队列加入一条消息。

消息输入可以有三种形式：

- JSON 形式：前后由 `{}` 包裹的字符串
- FILE 形式：由 `<-` 开头的字符串
- TEXT 形式：其他情况一律当作消息的文本形式

同时支持从管道获取：没有任何输入参数时，会读取管道内全部文本，根据内容判断是 JSON|TEXT|FILE 哪种形式。

# 用法

```bash
mq @send [消息1] [消息2] [-t 主题]
  # 消息    # 可多个，形式见上
  -t        # 主题，默认 sys
```

# 示例

```bash
# 加入一条 JSON 形式的消息
mq @send '{body:"echo xx > xx.txt"}'

# 加入一条 TEXT 形式的消息
mq @send '@user=demo\n\necho xx > xx.txt'

# 加入两条对象形式的消息
mq @send '<-id:xxx' '<-~/path/to/msg.txt'

# 通过管道添加消息
cat ~/msgs.txt | mq @send

# 指定主题
mq @send '{body:"hi"}' -t mytopic
```
