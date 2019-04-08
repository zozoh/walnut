# 命令简介 

    `mqttc  pub` 向一个topic发送一条消息

# 用法

```
mqttc $confName pub
		[topic]          # 目标topic,必选 
		[payload]        # 负载, 与文件(-f),管道(pipe) 三选一
		[-f xxx]         # 使用文件作为负载,不能超过8096. 为了减轻mqtt服务器的压力
		[~pipe]          # 管道输入
		[-qos 1]         # qos, 默认是0
```

## 示例


直接发送文本信息

```
mqttc mqttwk /btn/868575026630717/req hi
```

发送文件

```
mqttc mqttwk /btn/868575026630717/req -f xxx
```

从管道

```
cat xxx | mqttc mqttwk /btn/868575026630717/req
```