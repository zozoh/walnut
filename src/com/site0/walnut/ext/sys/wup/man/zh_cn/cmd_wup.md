# 命令简介

`wup` 用来提供通用更新服务：管理软件节点信息、构建和分发更新包。

数据主目录默认是 `~/wup`，可用 `-data` 指定。

# 用法

```bash
wup 操作 [子操作] [..参数] [-data 目录]
```

支持的处理器：

```bash
@init          # 初始化 wup 服务
@node_init     # 初始化一个节点
@node_get      # 获取节点信息
@node_query    # 获取节点列表
@node_set      # 设置节点信息（如软件版本）
@pkg_add       # 添加更新包
@pkg_build     # 构建更新包
@pkg_get       # 获取更新包（输出二进制数据）
@pkg_info      # 获取更新包信息
@srvconf_get   # 获取服务器配置
```

# 示例

```bash
# 初始化
wup init

# 初始化一个节点
wup node init -macid AABBCCDDEEFF -godkey 123456 -type gbox

# 获取节点信息
wup node get -macid AABBCCDDEEFF

# 获取节点列表
wup node query

# 设置节点的软件版本
wup node set -macid AABBCCDDEEFF -pkg walnut -version 8.0.0

# 构建更新包（all 包含 strato，all-site0 仅零站）
wup pkg build all
wup pkg build all-site0

# 添加更新包，可同时更新若干服务器的配置
wup pkg add ~/dw
wup pkg add ~/dw site0_qcloud site0_mediax cherry

# 获取更新包信息
wup pkg info -name jdk -version 8u112 -macid AABBCCDDEEFF -key adw..dsfasd

# 获取更新包（输出二进制数据）
wup pkg get -name jdk -version 8u112 -macid AABBCCDDEEFF -key adw..dsfasd
```
