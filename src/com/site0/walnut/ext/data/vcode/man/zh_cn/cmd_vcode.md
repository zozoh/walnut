# 命令简介

`vcode` 验证码校验

验证码由 `vcode create` 生成，存放在 `/var/vcode/<场景>/` 下。

# 用法

```bash
vcode @check 场景 验证码
```

支持的处理器：

```bash
@check    # 校验验证码，失败抛 e.cmd.vcode.check_fail
```

# 示例

```bash
# 校验登录验证码
vcode @check login/xiaobai 8834
```
