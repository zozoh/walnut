# 过滤器简介

`@ak` 判断指定 API 的密钥是否可以正常使用。

# 用法

```bash
xapi @ak 平台名 账号名 [-cqn]
       # 平台名    # 应用平台名称
       # 账号名    # 平台的账号名
  -cqn    # 输出的 JSON 格式化开关
```

# 示例

```bash
# 检查微信公众号的密钥是否可用
xapi @ak wxgh demo
# {
#   ok : true
# }
```
