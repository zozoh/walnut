# 命令简介

`whoisx` 提供对某个域名注册信息更友好的查询方式。

# 用法

```bash
whoisx 域名 [-cqn]
  -c    # JSON 紧凑显示
  -q    # 键名用双引号包裹
  -n    # 不忽略 null
```

# 示例

```bash
# 查询域名注册信息
whoisx nutzam.com
# 输出
# {
#     host : "nutzam.com",
#     phone : "xxxxx",
#     mobile : "xxxxx",
#     ...
# }
```
