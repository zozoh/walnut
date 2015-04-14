---
title: 任务列表
author:zozoh
---

* `OK` PS.parseToken 这个重新弄一下，总丢失转义字符可不是个事情
* `OK` 界面支持上下箭头翻看历史记录
* `OK` ls 命令，支持丰富的显示
* `OK` 提供 session -clear 命令：删除过期 session 的命令（包括 process）
* `OK` 纯文本编辑器（先用 textarea 简单替代一下）
* `OK` 支持文件上传
* 提供 groovy 的支持
* 打通 Gctp 的 ASK/Form 的流程
* 提供 API 机制，每个账号可以注册 API，并提供一行执行命令
* 提供 HTTP 资源访问模块，所有的 /home 下的 OBJ 都可以被访问
* 提供后台任务机制，定期执行一行命令
    - `schedule '-[ssMMdd ]' data >> xyz`    
* 提供 zDoc 的支持
* 提供 osync 数据的本地工具
* 提供 osync 数据的命令，以便从其他服务器同步 OBJ
* 提供 curl 命令
* 提供 find 命令
* 提供 ps 命令
* 实现权限
    - obj.mode
    - 命令 chmod, chgrp, chown
    - PrivilegeIo extends ZIo
* 提供 sudo 命令
* 提供 git 服务器的支持
* 提供自定义脚本的支持