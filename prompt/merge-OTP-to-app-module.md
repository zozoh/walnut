接下来，我们需要把 OTP 服务类融合进 

- `WnAppModuleSysLoginByPassword`
- `WnAppModuleDomainLoginByPassword`

两个实现类。

根据设计参考文档： `60-auth-otp.md`

我的思路是，我们需要在配置 `web.properties` 文件里指定配置项

```bash
# 启用 OTP 登录
#  - PASSWD_AND_OTP : 需要密码和 OTP 双重认证
#  - PASSWD_OR_OTP : 提供密码或者 OTP 均可
#  - OTP_ONLY : 仅用 OTP 认证
#  - PASSWD_ONLY : 仅用密码认证
# 默认：PASSWD_AND_OTP
app-module-sys-login=PASSWD_AND_OTP
app-module-dmn-login=PASSWD_AND_OTP
```

对于开发环境，可以在 `web_local.properties` 文件里覆盖配置项，便于开发
```bash
# 开发环境，不启用 OTP 登录
app-module-sys-login=PASSWD_ONLY
app-module-dmn-login=PASSWD_ONLY
```

因此两个模块就需要额外多两个参数

```java
// 用来创建 WnOTPService 实例
private String otpConfigName;
// 与 private String account; 一起使用，认证 OTP 代码
private String otpCode;
```

因此关键就是如何获取 `WnOTPService` 实例，它的关键是确定构造函数的第二个参数

```java
public WnOTPService(WnIo io, NutBean sessionVars) {
    this(io, sessionVars, "default");
}
```

也就是说 `otp` 的配置信息从哪里获取，我的策略是

- `WnAppModuleSysLoginByPassword` : `sessionVars= {HOME:"/root"}`
> 因此获取 `~/.otp` 就会从 root 用户的主目录去获取
- `WnAppModuleSysLoginByPassword` : `sessionVars= {HOME:site.getDomainHomePath()}`
> 因此获取 `~/.otp` 就会从对应 domain 的主目录去获取

如果构建 WnOTPService 实例失败，且还是需要 OTP 认证，那么就会报错

你看看我这个策略有什么需要补充的吗？
给我点建议