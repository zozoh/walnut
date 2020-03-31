# 命令简介 

`weixin jssdk` 用来获取 JS-SDK 配置信息

# 用法

```bash
weixin {ConfName} jssdk [-apilist] [url]
```

# 示例

```bash
# 采用默认的 JS-SDK 的配置信息
# 默认 URL 存在 wxconf.jsSdkUrl
# 默认的 api_list 存放在 wxconf.jsApiList 
demo@~$ weixin xxx jssdk

# 指定一个 URL 的 JS-SDK 配置信息
demo@~$ weixin xxx jssdk http://xxxx

# 指定了 api_list
demo@~$ weixin xxx jssdk -apilist ":aa,bb,cc,dd"

# 指定了 api_list (JSON)
demo@~$ weixin xxx jssdk -apilist "['aa','bb','cc','dd']

# 指定了 api_list (文件对象)
demo@~$ weixin xxx jssdk -apilist id:xxxx
```
