命令简介
======= 

`protobuf encode` 提供protobuf数据的编码
    
用法
=======

```    
protobuf encode [类名]             # 「必选」数据对应的protobuf类
                [-f 路径]   # 「可选」输入路径, 默认从标准输入读取
```

输入数据需要是JSON格式!

示例
=======

### 从标准输入读取,然后编码数据,并输出到标准输出
```
echo '{staeItems:[{id:1}, {id:1000, cno:123}], sateNum:20, sateType:"BD"}' | protobuf encode a9352.SateStatus
```

### 从文件读取,然后编码数据,并输出到标准输出
```
protobuf encode -f msg.json a9352.SateStatus
```

### 串行编码解码测试
```
echo '{staeItems:[{id:1}, {id:1000, cno:123}], sateNum:20, sateType:"BD"}' | protobuf encode a9352.SateStatus | protobuf decode a9352.SateStatus;
```