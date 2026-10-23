# 命令简介

`mount` 把扩展媒介挂载到目录（设置目录的 `mnt` 元数据）

不带参数列出所有挂载点（需 root 组成员）；两个参数时执行挂载
（需目标域管理员）。本地文件类映射（file 索引器/桶）仅限 root
组成员，且路径必须在 `mnt-file-allow` 白名单内。映射语法详见
`mount-mapping` 参考手册。

# 用法

```bash
mount                       # 列出全部挂载点（root）
mount -init                 # 重新同步 web.properties 里的 init-mnt 配置
mount 映射语法 挂载点目录
```

# 示例

```bash
# 列出所有挂载点
mount

# 挂载本地目录
mount file://~/workspace/data/abc ~/abc

# 重新挂载配置文件中的挂载点
mount -init
```
