# 命令简介 

`xapi wxjssdk` 专门为微信制作的 JSSDK 接口配置对象

> 参考微信文档： [附录1-JS-SDK使用权限签名算法](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html#62)

# 用法

```bash
xapi wxjssdk 
  [apiName]            #【必】应用名称（通常为 wxgh）
  [account]            #【必】 平台的账号名
  [reqName]            #【必】 平台的请求对象名称（通常为 jssdk）
  [-url http://xx]     #【必】 要签名的网页 URL
  [-force]             # 强制重新获取Access-Token
  [-proxy /path/to]    # 一个指向 proxy配置文件的路径
                       # @see man xapi send
  [-cqn]               # 输出的 JSON 格式化
```
# 示例

```bash
# 获取微信公众号用户的信息
demo@~$ xapi req wxgh funcook gh_user_info -url -vars 'openid:"xxx"'
https://api.weixin.qq.com/cgi-bin/user/info?access_token=xxx-xxx&openid=xxx&lang=zh_CN
```