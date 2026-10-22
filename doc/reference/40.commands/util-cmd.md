---
title: 工具命令
module: 40.commands
keywords: [base64, hexbin, btea, crc, jsonx, strx, seq, react, shorturl]
source: [ext/util/]
related: [command-model, data-cmd, jsonx]
---

# 工具命令

数据处理之外的小工具：编解码、加解密、JSON 管道、字符串处理、序列生成等。源码在 `src/com/site0/walnut/ext/util/`。

> 参数细节以 `man <命令>` 为准。

## 命令一览

- **`base64`** — 用途：base64 编码/解码（管道） — 手册：✅
- **`hexbin`** — 用途：二进制 ⇄ HEX 字符串 — 手册：✅
- **`btea`** — 用途：btea 加解密（管道） — 手册：✅
- **`crc`** — 用途：CRC 校验（hdl 子命令） — 手册：❌
- **`jsonx`** — 用途：JSON 过滤器管道（30+ @过滤器） — 手册：✅（30+ 子页）
- **`strx`** — 用途：字符串过滤器管道 — 手册：✅
- **`seq`** — 用途：序列生成器（create） — 手册：✅
- **`react`** — 用途：数据联动（@vars/@config/@happen） — 手册：✅
- **`shorturl`** — 用途：短链接生成（JVM Handler） — 手册：❌

## base64 / hexbin / btea

三个典型的「管道型」命令：从 stdin 读，向 stdout 写。

```bash
cat abc.bin | base64          # 编码
cat base.txt | base64 -d      # 解码

cat abc.bin | hexbin          # 二进制转 HEX
cat hex.txt  | hexbin -d      # HEX 转二进制

# btea 对称加密：密钥可用字符串 / base64 / hex 三种形式
echo 'secret data' | btea -k mykey | base64 > ~/cipher.txt
cat ~/cipher.txt | base64 -d | btea -k mykey -d
```

## jsonx：JSON 过滤器管道

`json` 命令的哥哥——不只格式化，还能对 JSON 结构做各种变换（`@过滤器` 管道，过滤器几乎都有独立手册页）：

```bash
echo '{a:1,b:2,c:3}' | jsonx @pick 'a,b'          # 只保留指定键
echo '{a:1,b:2}'   | jsonx @put '{d:99}'        # 合并设置值
echo '{x:{y:42}}'  | jsonx @get 'x.y'            # 取键路径
echo '[1,2,3]'     | jsonx @append '[4,5]'       # 列表追加
echo 'hello' -n    | jsonx -wrap say              # 包裹成 {say:"hello"}
```

常用过滤器分组：

- 全局：`@load @set @read @nil`
- 键控制：`@akeys @ikeys @pick @omit @remove @transkey`
- 结构变换：`@list2map @map2list @flatten @merge @put @append @prepend @item @filter @match @tab`
- 模板与校验：`@tmpl @translate @validate`

> 每个过滤器都有 `man jsonx <名>` 手册页，这是 Walnut 里手册最完整的命令之一。

## strx：字符串管道

```bash
echo -e 'a\nb\nc' | strx @join ,        # 多行连接成一个字符串
echo 'abc' | strx -trim                  # 去空白
```

过滤器（见 `man strx`）：`@test @join @prefix ...`——按条件输出、连接、加前缀等。

## seq：序列生成

为一批对象生成自增序列值（编号、单号）：

```bash
# 规则：先输入额外变量，再起始值，再模板，最后更新对象字段
# 详见 man seq
o ~/orders -l | seq create -fields 'no' -tmpl 'PO-${seq%06d}'
```

模板里可用变量：`seq`（当前序列值，可设起始/步长）、`obj`（当前对象，列表模式下有效）。

## react：数据联动

根据 JSON 配置 + 用户变量，对目标数据做联动修改——适合「改了 A 字段自动刷新 B 字段」的场景：

```bash
react @vars '{price:100,count:3}' @config ~/react/discount.json @happen
```

## crc / shorturl

- `crc`：CRC 校验码计算（hdl 子命令，见源码 `ext/util/crc`）；
- `shorturl`：短链接生成（见源码 `ext/util/shorturl`）。

## 典型场景

- **敏感字段落库前**：`btea -k` 加密 + `base64`；
- **API 组装响应**：查询结果 → `jsonx @pick/@omit` 裁剪字段 → 输出；
- **导入生成单号**：`seq create` 配合 `sheet` 导入；
- **把零散文本拼 JSON**：`strx @join` + `jsonx -wrap`。

## 延伸阅读

- [数据命令](data-cmd.md) — json / o / sqlx 等数据核心命令
- [命令模型](command-model.md) — 管道与过滤器机制
