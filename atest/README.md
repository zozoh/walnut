# atest — 本项目的集成测试

> 这份文件是写给**人**的：照着做，你在自己的控制台上就能把测试跑起来。
> 智能体的行为守则见 `atest/AGENTS.md`（如果项目里有）与 wn-doc 的 in-test 文档。

## 一分钟跑起来

前置条件（只做一次）：

1. **有一个 Walnut 实例在跑**（1 级 = IDE 里启动的，2 级 = 独立启动的）；
2. 在**工作区根或本项目根**放一份 `.wn-instances.json`，写清级别与端口：

   ```json
   { "L1": { "host": "127.0.0.1", "port": 8080 },
     "L2": { "host": "127.0.0.1", "port": 8090 } }
   ```

3. 确认 `atest.json` 里的 `instance` / `test_domain` 与你的环境一致；
4. **登录一次**（之后会话会被复用；失效时工具会自动删缓存，重新登录即可）：

   ```powershell
   python ../walnut/bin/wn-cli/wn-cli.py --level L1 login --usr demo --passwd 123456
   ```

日常（在项目根执行）：

```powershell
python atest/run.py
```

跑完看结果：

```powershell
python ../walnut/bin/at-report/at-report.py --dir .
# 打开 atest/report/latest.md
```

## 只跑几条

```powershell
python atest/run.py --only CASE-101              # 单条
python atest/run.py --only CASE-101,CASE-103     # 多条（逗号分隔）
python atest/run.py --stop-on-fail               # 遇到第一个红灯就停
```

## 只看格式、不执行

```powershell
python atest/run.py --check
```

适合改完用例先自检：字段缺没缺、`contracts` 是否够具体、期望里有没有易变字段。

## 换实例、重置数据

```powershell
python atest/run.py --instance L2        # 换到 2 级实例（覆盖 atest.json 的 instance）
python atest/run.py --reset              # 整批跑之前先执行 atest/cleanup.py
```

## 结果在哪

| 内容 | 位置 |
|---|---|
| 机器可读报告 | `atest/report/<时间戳>.json`、`atest/report/latest.json` |
| 人读报告 | `atest/report/<时间戳>.md`、`atest/report/latest.md` |
| 每例证据 | `atest/report/<时间戳>/<CASE-ID>/`（stdout、stderr、diff、对象快照） |

## 环境依赖

- **Python 3.10+**（`at-run` / `atlib` / `at-init` 均为标准库实现，**没有第三方依赖**）；
- 不需要 `pip install` 任何东西；如果你的机器上 `python` 指向 2.x，用 `python3` 或改 PATH；
- 跑测试**不需要** Java / Maven —— 只有构建 Walnut 本身才需要；
- Windows 上建议用 PowerShell；**Git Bash 用户**注意先 `export MSYS2_ARG_CONV_EXCL='*'`，
  否则 `~/path` 这类参数会被 MSYS 改写成 Windows 路径。

检查环境是否就绪：

```powershell
python --version                 # 3.10+
python atest/run.py --check      # 0 错 = 用例格式没问题
```

## 这个目录里有什么

```text
atest/
|-- run.py              总入口（本文件所在目录）
|-- README.md           本文件
|-- atest.init          静态夹具：测试前"世界该长什么样"（由 app init 执行）
|-- vars.json           夹具变量（可选；不要放密码）
|-- cases/<CASE-ID>.md  用例：人写意图，一例一文件
|-- scripts/<CASE-ID>.py 用例脚本：产出 actual
|-- baseline/<CASE-ID>.json 基线：审核者签过的期望值（可移植，不分实例）
|-- expect/<CASE-ID>.json   手写期望（expected: file 时用）
`-- report/             报告与证据（不进 git）
```

## 常见问题

**跑之前报"实例不可用"（退出码 2）**
实例没起、端口不对，或 `.wn-instances.json` 没找到。依次确认：实例在跑 → 端口对 → 文件在项目根或工作区根。

**报"基线缺失 / blocked"**
该用例声明了 `expected: baseline` 但还没建基线。先 `--record` 再 `at-baseline --from`，由**审核者**签发。

**`--check` 报 `baseline-stale`**
用例 md 或 `atest.init` 改了，基线需要重新审核（不是"随便刷一下"，要人看这个值还对不对）。

**红灯看不懂**
打开 `atest/report/<时间戳>/<CASE-ID>/summary.txt`：里面有一句话结论、diff 前三处、复现命令。

**手工跑 `scripts/*.py` 报 import 错误**
脚本要在 `at-run` 里跑（它注入 `PYTHONPATH` 与 `AT_*` 环境变量）。要手工调试，用
`python atest/run.py --only CASE-101`。

## 更多

- 用例怎么写：`at-init` 模板目录里的 `CASE-001.md` 与 skill 文档；
- 夹具怎么写：`atest.init` 顶部注释（语法与 `app init` 一致）；
- 计划与规则：`wn-plan` 仓库的 ATESET 计划。
