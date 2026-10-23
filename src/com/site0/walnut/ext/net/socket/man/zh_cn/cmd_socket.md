# 命令简介

`socket` 测试主机的指定端口是否可连通

# 用法

```bash
socket 主机 端口 [-timeout 毫秒]
  -timeout    # 连接超时（默认 1000ms）
```

# 示例

```bash
# 测试 80 端口
socket nutzam.com 80

# 自定义超时
socket db.internal 3306 -timeout 3000
```
