# 命令简介 

    `ftpd` 用来管理ftp服务

# 用法

```
ftpd passwd            # 创建或显示ftp密码, 强制更新密码加参数-n
ftpd status            # 显示ftpd服务状态
ftpd start             # 启动ftpd服务,仅root用户可用
ftpd stop              # 关闭ftpd服务,仅root用户可用
ftpd port [ftp-port]   # 设置ftpd服务的端口,仅root用户可用
```