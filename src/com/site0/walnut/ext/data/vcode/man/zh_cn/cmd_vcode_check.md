# 过滤器简介

`@check` 校验验证码是否正确。

验证码由生成端写入 `/var/vcode/<场景>/` 目录，本命令取 `<场景>` 下待校验的验证码与用户输入比对，校验失败抛出异常 `e.cmd.vcode.check_fail`。

# 用法

```bash
vcode @check 场景 验证码
```

# 示例

```bash
# 校验登录验证码
vcode @check login/xiaobai 8834
```
