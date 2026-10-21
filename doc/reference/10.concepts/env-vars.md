---
title: 环境变量
module: impl.box
keywords: [环境变量, env, PATH, HOME, .env, 会话变量]
source:
  - impl/box/cmd/cmd_env.java
  - web/WnConfig.java
related: [command-pipeline, domain-and-home, js-scripting]
---

# 环境变量

环境变量是会话级的名值对，命令行通过 `$NAME` / `${NAME}` 引用，JS 里通过 `sys.envs` 访问。它承担三类职责：**路径查找（PATH/APP_PATH）、行为开关（THEME/OPEN）、业务上下文（自定义变量）**。

## 变量从哪来（优先级从低到高）

1. **系统默认**：`usr-dft-meta`（web.properties）给新用户配的初始元数据
2. **用户持久化**：`~/.env` 文件（MultiLineProperties 格式），登录时加载
3. **会话动态**：命令 `env set` / `SET` 修改，仅存于会话
4. **强制覆盖**：`!NAME` 前缀（站点 `env` 元数据里 `!HOME` 强制设置，无视前序值）

会话对象的内容 JSON 保存了环境变量，因此会话恢复时变量原样回来。

## 关键变量

| 变量 | 含义 | 示例值 |
|------|------|--------|
| `HOME` | 主目录（`~` 展开值） | `/home/demo` |
| `PWD` | 当前目录 | `/home/demo/docs` |
| `PATH` | 命令查找路径 | `/bin:/sbin:~/bin` |
| `OPEN` | 登录后打开的应用 | `wn.console` |
| `APP_PATH` | 应用查找路径 | `/rs/ti/app:/app` |
| `THEME` | 界面主题 | `light` |
| `QUIT` | 登出跳转 URL | `/a/login` |

## 操作命令

```bash
env                    # 显示全部环境变量
env get PATH           # 取一个值
env set A=100 B=hello  # 设置（值里的 $X 和 ~ 会被展开）
env rm A,B             # 移除
env clear              # 清空
env save               # 存到 ~/.env（持久化）
env load               # 从 ~/.env 重新加载
echo $PATH             # 命令行直接引用
```

`~/.env` 文件长这样：

```properties
HOME=/home/xiaobai
PATH=$PATH:~/bin
TMPDIR=$HOME/.tmp
MY_KEY=abc123
```

## 会话级 JS 访问

```js
// jsc 脚本里
sys.envs.get("HOME");
sys.envs.set("MY_KEY", "xyz");
```

## 常见场景

```bash
# 场景 1：给用户加一个自定义命令目录
env set PATH=$PATH:~/mybin
env save

# 场景 2：API 里按环境变量分流
echo "${MODE=demo}"        # 未设置时用默认值 demo

# 场景 3：临时会话变量（不保存）
env set DEBUG=true
```

## 注意事项

- 变量名大小写敏感；`env set` 的值是普通字符串，`$X`/`~` 在设置时展开。
- 会话有默认时长（`se-sys-du`，默认 3600 秒），过期后环境变量随会话消失——要持久化必须 `env save` 到 `~/.env`。
- 域站点登录时，站点目录 `env` 元数据会覆盖系统默认变量（`!` 前缀强制）。
