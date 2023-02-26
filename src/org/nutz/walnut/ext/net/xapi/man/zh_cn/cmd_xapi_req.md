# 命令简介 

`xapi req` 获取向第三方平台请求的对象内容

# 用法

```bash
xapi req 
  [apiName]        # 应用平台名称
  [account]        # 平台的账号名
  [reqName]        # 平台的请求对象名称（具体参见文档 f0-xapi-weixin.md）
  [-vars {..}]     # 参数表的上下文，如果没有，从标准输入读取
  [-cqn]           # 输出的 JSON 格式化
  [-url]           # 仅仅输出请求的 URL （会自动拼上基础路径和参数）
  [-curl]          # 输出 curl 调用的命令
  [-N]             # 输出 curl 命令换行
  [-cache]         # 仅仅输出请求的缓存键
```
# 示例

```bash
# 获取微信公众号用户的信息
demo@~$ xapi req wxgh funcook gh_user_info -url -vars 'openid:"xxx"'
https://api.weixin.qq.com/cgi-bin/user/info?access_token=xxx-xxx&openid=xxx&lang=zh_CN
```