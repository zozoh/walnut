# 命令简介 

`xapi ak` 判断指定 API 的密钥是否可以正常使用

# 用法

```bash
xapi ak
  [apiName]        # 应用平台名称
  [account]        # 平台的账号名
  [-cqn]           # 输出的 JSON 格式化
```
# 示例

```bash
# 获取微信公众号用户的信息
demo@~$ xapi ak wxgh demo
{
  ok : true
}
```