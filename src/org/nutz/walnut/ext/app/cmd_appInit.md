# 命令简介 

`app` 命令用来辅助用户快速构建一个应用
  
# 初始化数据目录

每个应用都会可能会有自己的数据目录，通过

```
app-init [-file /path/to/input]    # 输入，没有的话，从管道读
         [/path/to/data]           # 处理的数据目录
         [-pnb gh_xxxxx]           # 微信公众号
         [-host 127.0.0.1:8080]    # 主机地址，可以是IP或者域名加上端口			   
```

* 可以对其初始化。 如果不给参数，那么默认初始化当前目录
* 初始化的信息会从管道读取，也可以从一个本地文件读取 `-file`
* 信息的格式可以是一段属性文本(见下面的说明)


```
#
# 一个较长的名字，类似 Java 的类路径，标识这个应用
#
appid=com.my.app
#
# 应用的名称，最好不要包含空格反斜杠，总是符合正则式 ^([0-9a-zA-Z_-]+)$ 就好
# 默认的，会采用 appid 的最后一段名称
#
appnm=MyAppName
#
# 定义了数据目录
#
data:
@DIR .
  - ?attr_a = xxxx     # 属性用 ? 开始，则表示只有没有才设定
  - attr_b  = xxxx

@DIR user
  - uiForm    = jso:///a/load/${appid}/ui_user_form.js
  - uiAdaptor = jso:///a/load/${appid}/ui_user_main.js


@FILE myconf
------------------------------------------ begin
这里是文件的内容
直到遇到结束标记为止
所谓的结束标记就是一行不少于 6 个 - 加上 beign/end 
文件的内容，支持转义字符 \# 和  \@ 分别表示 @ 和  #
------------------------------------------ end
#
# 定义了 httpapi 的接口
# % 开头的表示对于全部 api 都添加的特性，现在支持
#    %across-domain
#
httpapi:
%across-domain

@${appnm}/user/get
  + Content-Type = text/plain

> echo abc

@/api/2
> obj * -json -l


# ~end of httpapi
#
# 定义了微信控制器，这个需要额外参数 -pnb 来指定微信的公众号 ID
# 每条微信钩子的命令模板都支持占位符:
#   ${appnm}
#   ${appid}
#   ${host}  - 服务器地址， 通过 -host 参数获得
#   ${grp}   - 组，通过当前的会话获得
#   ${pnb}   - 微信公众号，通过 -pnb 参数获得
#
wxhook:

# 第一行标识了钩子的 ID ，后面的 ":true" 可选表示微信消息带上下文
# 紧接着是匹配条件，是一个 JSON 字符串 
# 一直读取到 > 开头的行，则表示命令
# 命令行可以是多行，每行之间自动加入空格连接，如果不想加入空格，行尾用 '\' 结尾即可
@my_luck:true
['抽奖', {'EventKey' : 'LUCK'}]

> echo "hello"
> weixin -out 'article:赢大奖，送豪礼;;
             点击进入抽奖界面;;
             http://${host}/${appnm}/p/${grp}/${pnb}/$${weixin_FromUserName}'
             -inmsg id:$${id}

@my_next
{...}

> echo "haha"

# ~end of weixin
```
