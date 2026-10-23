# 命令简介

`exit` 登出当前会话，并输出新会话的 JSON

命令会销毁当前会话并创建匿名新会话（Walnut 不允许没有会话），输出 AjaxReturn
JSON（data 为新会话对象）。同时向沙盒写入换会话宏，HTTP 模块据此让客户端
更换票据。

# 用法

```bash
exit [-cnqH]
  -c    # JSON 紧凑输出
  -n    # 输出 null 值字段
  -q    # 键名加引号
  -H    # 隐藏 ^__ 开头的字段
```

# 示例

```bash
# 登出
exit

# 紧凑输出
exit -c
```
