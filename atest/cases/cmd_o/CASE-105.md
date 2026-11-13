---
caseID: CASE-105
title: 命令通道建子目录、echo 写文件、@read 按路径读回内容
source: man cmd_o §@create §@read、man cmd_echo
contracts:
  - cmd_o @create
  - cmd_echo -n > ~/atest-lab/repro/hello.txt
  - cmd_o @read @json -cqn
expected: inline
---

# CASE-105 命令通道建子目录、echo 写文件、@read 按路径读回内容

## 前置

- 依赖 suite 夹具 `atest/init/cmd_o.init` 铺好的 `~/atest-lab`（本例不新增夹具、
  不改 `init/`）。
- 本例新增的动态数据只有两件：目录 `~/atest-lab/repro/` 与该目录下的文件
  `hello.txt`（内容固定为 `atest-repro-content`）。
- 幂等：目录与文件对象都用 `-upsert` 创建，文件内容用 `echo -n … >` 覆盖写；
  重复执行不产生第二份对象，也不动 `~/atest-lab` 下别的数据。

## 步骤

1. 铺夹具：`atlib.fixture()`（suite 入口 `atest/init/cmd_o.init`）；
2. 在命令通道执行，建目录：

   ```bash
   o @create -p ~/atest-lab -race DIR -upsert '{"nm":"repro"}'
   ```

3. 在命令通道执行，建文件对象：

   ```bash
   o @create -p ~/atest-lab/repro -race FILE -upsert '{"nm":"hello.txt"}'
   ```

4. 在命令通道执行，写文件内容（`-n` 不加行尾换行）：

   ```bash
   echo -n atest-repro-content > ~/atest-lab/repro/hello.txt
   ```

5. 在命令通道执行，按路径把内容读回对象（`@read` 把内容放进 `content` 键，
   `@json -cqn` 输出严格 JSON）：

   ```bash
   o ~/atest-lab/repro/hello.txt @read @json -cqn
   ```

## 期望

```json
{
  "nm": "hello.txt",
  "race": "FILE",
  "mime": "text/plain",
  "len": 19,
  "content": "atest-repro-content"
}
```

## 证据

- `objects/~__atest-lab__repro__hello.txt.json`：`snap()` 抓的对象快照
  （`nm` / `race` / `mime` / `len` 的来源）。
- `summary.txt`：脚本 `note()` 记录的读回内容与对象长度。
- 各步命令的 stdout / stderr 与 `cmd.txt`：命令通道的实际调用。
- `result.json`：脚本 `emit()` 交出的 actual（含 `content`）。
