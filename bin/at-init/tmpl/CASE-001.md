---
caseID: CASE-001
title: 新建目录后按路径能读到
source: ATESET / man cmd_o §@create
contract: cmd_o @create
expected: inline
---

# CASE-001 新建目录后按路径能读到

> **本文件就是一个合法用例，照抄即可**：把它复制成 `test/cases/<CASE-ID>.md` 后改三处——
> 文件名、`caseID`、正文一级标题里的 ID 与 `title`（两处同文）；再按需改下面四节。
> 选填字段：`instance`（缺省取 `ATEST.md` 的 instance）、`suite`（缺省 `default`）、
> `owner`、`created`；不写就是缺省，不要为了"填满"硬写。
> 字段与校验规则的权威定义见 ATESET-02 任务书；写作套路见 `write-test-case` skill。

## 前置

- 静态布局见 `test/atest.init`：域 home（`~`）已存在；本例不依赖任何 `.dao` / `.regapi` 接口。
- 本例只新增动态数据 `~/atest-dir` 一个目录；用 `-upsert` 保证重复执行不报错，无独立清理脚本。

## 步骤

1. `python bin/wn-cli/wn-cli.py cmd 'o ~ @create atest-dir -race DIR -upsert'`
   —— 在域 home 下创建目录对象（级别由 runner 按 `ATEST.md` 注入；手工复跑时补 `--level L1`）；
2. `python bin/wn-cli/wn-cli.py cmd 'o ~/atest-dir @json -cqn'`
   —— 按路径读回该对象，原始 JSON 落到证据目录。**必须带 `-cqn`**：裸 `@json` 是 HOCON 风格
   （键不加引号），脚本没法直接 `json.loads`。

## 期望

只比下面 json 里出现的键；`id` / `pid` / `ct` / `lm` / `sha1` 每次执行都不同，不写、不比。

```json
{
  "nm": "atest-dir",
  "race": "DIR"
}
```

## 证据

默认三件，失败时由脚本按本节抓取（够用就不必改这一节）：

1. 两步 `wn-cli` 的原始 stdout / stderr 与退出码；
2. `o ~/atest-dir @json` 的完整对象快照；
3. 该会话、该路径对应的 `access.log` 片段。

> 需要额外快照（例如 `~` 的子节点列表）或前后时间区间时，在本节加一条，写明取什么、存哪。
