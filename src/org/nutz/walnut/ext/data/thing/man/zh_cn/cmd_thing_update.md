# 命令简介 

`thing update` 更新 Thing 的元数据

# 用法

```bash
thing [TsID] update 
  [ID1 ID2 ...]          # 多个对象，空格分隔
  [-fields "{..}"]       # 自由字段
  [-match "{..}"]        # 更新前检查数据是否匹配
                         # 这个 match 就是一个 AutoMatch
  [-safe {..}]           # 一个json: {acived:REGEXP,locked:REGEXP}
                         # 表示更新时哪些字段会被无视，相当于更新保护
                         # - active:  如果声明，只有被这个正则匹配的字段
                         # 才会被更新，譬如 '^(name|age)$'
                         # - locked:  如果声明，只有不被这个正则匹配的字段
                         # 才会被更新，譬如 '^(ct|lm)$'
  [-nohook]              # 打开这个开关，则运行时将无视onUpdate 等回调设定
  [-l]                   # 如果只有一个对象，也要强制输出数组
  [-cqn]                 # JSON 格式化方式
#----------------------------------------------------
```

- 当前对象必须是一个 thing，否则不能更新
- fields 里面的值，没有 `-brief|ow|cate` 优先

# 示例

```bash       
# 修改信息
thing xxx update 45ad..6823 -fields "x:100,y:99"
```