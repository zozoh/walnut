---
title: 命令输入输出
module: impl.box
keywords: [命令, 标准输入, 标准输出, 重定向, stdin, stdout, stderr, JvmBoxInput, JvmBoxOutput]
source:
  - impl/box/JvmBoxInput.java
  - impl/box/JvmBoxOutput.java
  - impl/box/JvmCmd.java
  - impl/box/JvmAtomRunner.java
  - util/Cmds.java
related: [../10.concepts/04-command-model.md, custom-command, o, sqlx]
---

# 命令输入输出

每个命令运行时有三个标准流，行为与 Linux Shell 一致：

- **stdin**（`sys.in`，`JvmBoxInput`）：读入数据，默认来自管道或会话输入
- **stdout**（`sys.out`，`JvmBoxOutput`）：正常输出，可被管道消费或重定向到文件
- **stderr**（`sys.err`，`JvmBoxOutput`）：错误输出，默认直接送调用方，不进管道

## 读取输入

```java
// 一次性读完管道内容（最常用）
String text = sys.in.readAll();

// 按行读
String line = sys.in.readLine();

// 读到字节流（二进制，如图片处理）
InputStream ins = sys.in.getInputStream();
```

惯例是**优先用参数 / 文件，都没有才读管道**（`Cmds.getParamOrPipe` 封装了这个优先级）：

```bash
# 三种来源等价：参数、文件、管道
tmpl @vars '{name:"wendal"}'
tmpl @vars -f ~/vars.json
echo '{name:"wendal"}' | tmpl @vars
```

注意：管道内容只能读一次。前置过滤器已经读过的话，后续拿到的是空。

## 输出

```java
sys.out.println("hello");        // 一行文本
sys.out.printlnf("%s=%d", k, v); // 格式化
sys.out.write(ins);              // 字节流（如 cat 输出文件内容）
sys.out.printJson(obj, jfmt);    // JSON 输出（按全局开关格式化）
sys.err.println("e.xxx : 出错原因"); // 错误信息走 err
```

**stderr 不进管道**——这让 `cmd1 | cmd2` 时，前者的错误信息不会被后者当数据处理。
错误文本约定以 `e.` 前缀开头（如 `e.io.obj.noexists`），`ajaxre` 等命令靠它判断异常。

## 输出格式惯例

JSON 输出的格式化开关由 `Cmds.gen_json_format(params)` 统一生成（`hc.jfmt` / `fc.jfmt`）：

```bash
-c        # 紧凑输出（无缩进）
-q        # 键名不加引号
-n        # 忽略 null 字段
-json     # 输出 JSON 文本（针对对象命令）
-ajax     # Ajax 返回包裹 {ok, data}
-t f1,f2  # 指定输出字段
-l        # 强制列表输出
-quiet    # 不输出（配合批量）
```

细节以各命令手册为准。

## 重定向

```bash
# stdout 覆盖写入对象（路径或 id:）
echo "hello" > ~/docs/a.txt
# stdout 追加写入
cat ~/logs/app.log | grep ERROR >> ~/archive/errors.txt
# stderr 合并到 stdout（一起进管道或文件）
cat ~/a.txt 2>&1
```

重定向由 `JvmCmd` 解析，规则：

- `>` 覆盖、`>>` 追加，目标是一个 **Walnut IO 对象路径**（不是本地文件）
- 目标对象不存在时自动创建（`WnRace.FILE`）
- 带重定向的命令不会把自己的输出再给管道（它已经是管道的终点）
- 只支持 `>` `>>` `2>&1`，**不支持 `<` 输入重定向**——输入请用管道

## 在 HTTP API 里

regapi（`.regapi` 文件）执行命令时，三个流映射到 HTTP：

- stdin ← 请求体
- stdout → 响应体
- 错误会被包装成 HTTP 错误响应（或 AjaxReturn，配合 `ajaxre`/`httpout`）

因此写 API 时只需关心 stdout 输出什么——这正是 Walnut「命令即 API」的基础
（参见 [API 对象与元数据](../50.regapi/api-object.md)）。
