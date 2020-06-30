# 命令简介 

`thing delete` 删除输入
    
# 用法

```bash
thing [TsID] delete   
  [ID1, ID2...]          # 多个ID
  [-hard]                # 硬删除
  [-l]                   # 返回输出时一定保持列表
  [-match "{..}"]        # 更新前检查数据是否匹配
                         # 这个 match 就是一个 NutMap.match
```

- 当前对象必须是一个 thing，否则不能删除
- 已经删除的（th_live=-1），再次删除会彻底删除这条记录，包括其数据目录
- `hard` 开关表示硬删除，即，即使 th_live=1也要彻底删除
- 所谓删除其实就是标记 th_live = -1
- 参数 `-l` 表示是否强制结果输出成数组，否则只有一个 ID 的时候输出的是一个对象

