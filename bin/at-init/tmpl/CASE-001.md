# CASE-001 <一句话：被测行为>

> 样例用例模板（`bin/at-init/tmpl/`）：复制成 `test/cases/<CASE-ID>.md` 后改写。
> 字段以 ATESET-02 定稿为准；头五项是必填。

- 需求来源: <计划 ID / 需求 / man 页，如 `man cmd_o`>
- 被测契约: <命令或 API，如 `o / @json`>
- 作者: <智能体代号>
- 实例: 继承 ATEST.md 的 instance（或写死 L1 / L2）
- 期望来源: 内联 expected（内联 expected / 独立期望文件 / 基线快照，三选一）

## 前置

- 静态布局见 `test/atest.init`；本例只新增下列动态数据，脚本自带清理，可重复跑。

## 步骤

1. `test/scripts/CASE-001.py` 执行被测契约，原始响应落到证据目录；
2. 结果写进测试域（对象树），由 at-run 断言。

## 期望

```json
{
  "ok": true
}
```

## 证据

- 报告需带：执行的命令、原始响应、对象树快照路径、运行时间区间。
