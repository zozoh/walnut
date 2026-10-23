# 命令简介

`session` 查看会话信息

不带参数显示当前会话；`-list` 列出系统全部会话（需 root 组管理员）；
`-site`/`-host` 列出指定站点/域名的会话（需站点管理员）；位置参数为
查询条件（JSON）。

# 用法

```bash
session [条件JSON...] [-list] [-site 站点] [-host 域名]
        [-sort '{..}'] [-limit 50] [-skip 0] [-tab 列...] [-cqnbish]
  -list     # 系统会话列表（root 组管理员）
  -site     # 按站点（id:xxx 或路径）列会话
  -host     # 按域名列会话
  -sort     # 排序，如 '{ct:-1}'
  -limit/-skip   # 分页
  -tab      # 表格输出，指定列（如 createTime,ticket,type,unm）
  -bish     # 表格边框/表头/脚注
  -cqn      # JSON 格式开关
```

# 示例

```bash
# 显示当前会话
session

# 按条件查询会话
session '{unm:"xiaobai"}'

# 列出全部系统会话（表格，需 root 管理员）
session -list -tab createTime,ticket,type,unm -bish

# 某站点的会话列表
session -site ~/www/login
```
