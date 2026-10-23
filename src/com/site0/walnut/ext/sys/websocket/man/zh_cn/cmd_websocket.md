# 命令简介

`websocket` 用来处理 WebSocket 相关的操作：向在线的 WebSocket 会话发送文本、二进制、事件、ping，或列出/清理会话。

消息目标可以是某个 `wsid`，也可以是 `id:xxx`（对象 ID），此时发给监视该对象的全部 WebSocket。

# 用法

```bash
websocket @text|@binary|@event|@ping 目标 [内容] [-callback 命令]
websocket @list [条件JSON] [-skip N] [-limit N] [-sort 排序] [-clean] [-quiet]
websocket @check [-y]
```

支持的处理器：

```bash
@text     # 发送文本消息；内容来自参数或管道
@binary   # 发送二进制消息；内容来自管道
@event    # 发送事件（文本消息的封装：{event:...}+附加字段）
@ping     # 发送 ping 帧保活
@list     # 列出所有 WebSocket 对象，可附带清理无效会话
@check    # 检查对象上残留的 WebSocket 键；-y 时清除
```

# 工作流程

```bash
# 客户端发起连接
# 服务器接受连接，下发文本欢迎信息 {event:"hi",wsid:'$wsid'}
# 客户端发送监视请求 {method:'watch', user:'$user', match:{box_macid:'AABBCCDDEEFF'}}
# 客户端定期发送 ping 保活
# 服务器端可在任意时刻用 websocket 命令向指定 wsid 发消息
```

# 示例

```bash
# 发送文本消息
websocket @text 4tqpgk7u0ch3jovd9fhvg0cg5i "{method:'snapshot'}"

# 发送给监视某对象的全部 WebSocket
websocket @text id:xxx "{method:'snapshot'}"

# 发送事件（附加字段可选）
websocket @event 4tqpgk7u0ch3jovd9fhvg0cg5i publish

# 发送二进制流
cat abc | websocket @binary 4tqpgk7u0ch3jovd9fhvg0cg5i

# 列出 WebSocket 对象并清理无效会话
websocket @list -clean
```
