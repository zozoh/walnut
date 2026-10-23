# 过滤器简介

`@wxjssdk` 专门为微信制作的 JSSDK 接口配置对象（签名等）。

> 参考微信文档：[附录1-JS-SDK使用权限签名算法](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html#62)

# 用法

```bash
xapi @wxjssdk 平台名 账号名 请求名 -url 网页URL [-force] [-proxy 路径] [-cqn]
       # 平台名    # 应用名称（通常为 wxgh）
       # 账号名    # 平台的账号名
       # 请求名    # 平台的请求对象名称（通常为 jssdk）
  -url      # 要签名的网页 URL（必填）
  -force    # 强制重新获取 Access-Token
  -proxy    # 指向 proxy 配置文件的路径，@see `man xapi @send`
  -cqn      # 输出的 JSON 格式化开关
```

# 示例

```bash
# 生成 JSSDK 配置对象
xapi @wxjssdk wxgh funcook jssdk -url 'http://demo.com/page' -cqn
```
