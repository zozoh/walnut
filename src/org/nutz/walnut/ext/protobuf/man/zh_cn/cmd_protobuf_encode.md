命令简介
======= 

`protobuf decode` 提供protobuf数据的解码
    
用法
=======

```    
protobuf decode [类名]             # 「必选」数据对应的protobuf类
                [-f 路径]   # 「可选」输出路径, 默认输出到标准输出
```

示例
=======

### 解码数据,并输出到标准输出
```
cat msg.data | protobuf decode a9352.SateStatus
```

### 解码数据,并输出到文件
```
cat msg.data | protobuf decode a9352.SateStatus -f msg.json
```

### 串行编码解码测试
```
echo '{staeItems:[{id:1}, {id:1000, cno:123}], sateNum:20, sateType:"BD"}' | protobuf encode a9352.SateStatus | protobuf decode a9352.SateStatus
```