# 命令简介 

`thing update` 更新 Thing 的元数据

# 用法

```bash
thing [TsID] update 
  [ID1 ID2 ...]          # 多个对象，空格分隔
  [-fields "{..}"]       # 自由字段
  [-match "{..}"]        # 更新前检查数据是否匹配
                         # 这个 match 就是一个 AutoMatch
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