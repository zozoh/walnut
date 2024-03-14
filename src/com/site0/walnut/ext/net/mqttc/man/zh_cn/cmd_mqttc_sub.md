# 命令简介 

    `mqttc sub` 订阅topic,并注册处理文件

# 用法

```
mqttc $confName sub 
		[topic]          # 指定一个topic,必选
		[handlerName]    # 处理器名称, 位于 ~/.mqttc/$confName/handler/$handlerName
```

## 示例

```
mqttc mqttwk sub /btn/868575026630717/report msgin
```