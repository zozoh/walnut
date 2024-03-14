# 命令简介 

    `sshd` 用来管理ssh服务

# 用法

```
sshd passwd            # 创建或显示ssh密码, 强制更新密码加参数-n
sshd status            # 显示sshd服务状态
sshd start             # 启动sshd服务,仅root用户可用
sshd stop              # 关闭sshd服务,仅root用户可用
sshd port [sshd-port]   # 设置sshd服务的端口,仅root用户可用
```