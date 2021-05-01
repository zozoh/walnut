# 命令简介 

    `mqttc` mqtt客户端操作(mqttc=mqtt client)

# 用法

```bash
mqttc load               # 加载配置信息
mqttc pub                # 发布一条信息
mqttc sub                # 订阅一个topic
mqttc unsub              # 取消订阅一个topic
```

配置目录

```bash
~/.mqttc
	- mqttwk           # 配置命名
		- conf          # 配置文件,内容是json
		- message       # sub消息缓存,有效期10分钟,自动创建
			- abc...def  # 某条消息
			- def...abc  # 第二条消息
		- handler
			- msgin      # 处理器名称, mqttc sub的时候会用到, 需要是jsc文件
		- persistence   # pub消息的持久化,自动创建
			- abc...def  # 某条消息
			- def...abc  # 第二条消息
```

配置文件格式

```json
{
	"url" : "tcp://mqttwk.nutz.io:1883", # 单一服务器地址
	"serverURIs" : "tcp://..,tcp://..",  # 多服务器地址,比url优先, 两者必须填一个
	"username" : "abc",                  # 用户名,可选
	"password" : "abc",                  # 密码,可选
}
```