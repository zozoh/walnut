# 命令简介 

`thing update` 更新 Thing 的元数据

# 用法

```bash
thing [TsID] update ID 
  ["$th_nm"]             # 标题
  [-brief "xxx"]         # 摘要
  [-ow "xxx"]            # 所有者
  [-cate CateID]         # 分类
  [-fields "{..}"]       # 自由字段
  [-match "{..}"]        # 更新前检查数据是否匹配
                         # 这个 match 就是一个 AutoMatch
#----------------------------------------------------
```

- 当前对象必须是一个 thing，否则不能更新
- fields 里面的值，没有 `-brief|ow|cate` 优先

# 示例

```bash    
# 改名
thing xxx update 45ad6823.. "原力觉醒电影票"
    
# 修改简介
thing xxx update 45ad6823.. -brief "会员半价"
    
# 修改更多的信息
thing xxx update 45ad6823.. -fields "x:100,y:99"
```