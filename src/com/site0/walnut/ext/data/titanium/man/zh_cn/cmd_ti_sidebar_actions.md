# 过滤器简介

`@sidebar_actions` 根据 sidebar 的 json 内容，生成 action 文件列表。

侧边栏内容来自管道；如果管道为空，则自动执行 `ti sidebar -qn` 获取当前会话的侧边栏。

# 用法

```bash
ti @sidebar_actions [-cqn]
  -cqn    # JSON 输出的格式化开关
```

# 示例

```bash
# 从当前会话侧边栏生成 action 列表
ti @sidebar_actions

# 从管道读取侧边栏 JSON
cat ~/mysidebar.json | ti @sidebar_actions
```
