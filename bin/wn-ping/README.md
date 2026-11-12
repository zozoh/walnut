# wn-ping — Walnut 探活

探测目标实例的 `GET /a/login_mode`，输出 JSON 结论。契约见
`{WN_DOC_HOME}/reference/102.auto-test/30-at-tools.md`（`{WN_DOC_HOME}` = 工作区同级 `../wn-doc/`）。

```bash
# 探活（默认 localhost:8080）
python wn-ping.py
# → {"ok":true,"alive":true,"url":"...","system":"PASSWD_ONLY","domain":"PASSWD_ONLY","agent_ready":true}

# 指定地址与超时
python wn-ping.py --url http://localhost:8090 --timeout 5
```

退出码：`0` 活着；`2` 探活失败；`1` 参数错误。

`agent_ready=false` 表示实例活着，但登录模式不允许纯密码（`PASSWD_AND_OTP` 或 `OTP_ONLY`），
不满足智能体测试前提。`PASSWD_ONLY` 与 `PASSWD_OR_OTP` 都算可用。
