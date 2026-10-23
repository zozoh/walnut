# 命令简介

`grp` 管理用户组：查询组员、添加/删除成员

只有本组管理员或 root 组成员才能增删成员。不带参数显示当前用户所在的组；
带组名显示该组的成员列表。

# 用法

```bash
grp [组名] [-a 用户] [-role 角色值] [-d 用户] [-get 用户] [-json] [-quiet]
  -a      # 添加用户到组（缺省为当前用户）
  -role   # 添加时的角色：1 管理员 / 10 组员（默认）/ 100 预备组员 / -1 黑名单
  -d      # 从组中删除用户
  -get    # 查询某用户所在的组（缺省为当前用户）
  -json   # 以 JSON 数组输出（默认文本表格）
  -quiet  # 出错时不抛异常
```

# 示例

```bash
# 显示当前用户所在的组
grp

# 显示某组的成员列表
grp nutz

# 将自己添加为组员（需该组管理员权限）
grp nutz -a

# 添加组管理员
grp nutz -a -role 1

# 将某用户加入黑名单
grp nutz -a xiaobai -role -1

# 从组中删除某用户
grp nutz -d xiaobai

# 查询某用户所在的组（JSON）
grp -get xiaobai -json
```
