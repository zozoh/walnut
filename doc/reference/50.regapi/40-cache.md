---
title: API 缓存（cache-*）
module: 50.regapi
keywords: [cache-test, cache-duration, cache-finger-key, cache-lazy, cache-redirect, 302, 缓存]
source: [ext/sys/httpapi/HttpApiModule.java]
related: [10-api-object, 30-path-params, 50-auth-history]
---

# API 缓存（cache-*） 概述
有些接口被频繁调用，但查询的内容很久才会变——每次都执行命令，白白浪费 CPU 和 IO。
regapi 自带**页面级缓存**：把命令的产出物缓存成一个文件对象，后续相同请求直接命中，
跳过命令执行。适合报表、列表页、静态化内容这类「读多写少」的接口。

## 工作原理

1. 请求到达后先执行 `cache-test` 匹配；
2. 命中且未过期 → 直接输出缓存对象（或 302 重定向），命令不执行；
3. 未命中 → 正常执行命令，执行完把产出写到缓存路径（懒加载模式 `cache-lazy`，默认开），下一次请求命中。

**以下请求无视缓存**（源码硬规则）：

- 非 GET 请求（POST/PUT/...）；
- 需要站点鉴权的请求（`http-www-auth`；声明了 `http-www-home` 即默认需要鉴权）；
- 声明了 `http-dynamic-header` 的 API。

## cache-* 属性详解

### `cache-test`：匹配条件

- 类型：NutMap 数组，**逐项尝试、先中先得**；每项三个键：

```json5
{
  cache-test : [
    { path : "/var/cache/report/today.json",
      mime : "application/json",
      match : [ { "day" : "today" } ] },
    { path : "/var/cache/report/${params.uid}.json",
      mime : "application/json",
      match : { "tp" : ["hot","new"] } }
  ]
}
```

- `match`：匹配条件，**只对查询参数**做匹配（键写参数原名，如 `day`；
  数组为 AND，见 [动态校验语法](#动态校验语法)）；缺省总匹配
- `path`：缓存文件路径模板，渲染上下文为请求元数据
  （可用 `${http-qs-*}`、`${params.*}`）；缺省或为空串的项被跳过
- `mime`：命中时的响应 Content-Type

### `cache-duration`：有效期

- 类型：int（秒）；`0` 或缺省 = 永久有效
- 按缓存文件的最后修改时间判定：太老即视为失效，重新执行命令

### `cache-finger-key` / `cache-finger-as`：请求指纹

- 有些接口同一路径会因参数不同产出不同内容（如 `?day=20261020` 与 `?day=20261021`）
- 设置 `cache-finger-key` 后，系统对**原始查询串**计算摘要（默认算法 MD5，
  可用 `cache-finger-as` 换成 SHA1 等），命令执行完后写入缓存对象的该键；
  后续请求摘要不一致即视为缓存失效——把参数变化纳入缓存校验，防止串数据
- 缺省不校验指纹

### `cache-lazy`：懒加载

- 类型：bool；默认：`true`
- 未命中时正常执行命令，stdout 一边响应一边写入缓存路径，下一次请求即命中；
  设为 `false` 则只读缓存、不自动生成

### `cache-redirect`：命中后 302

- 类型：string（URL 模板）
- 命中时不输出缓存内容，而是 302 跳转到渲染后的 URL；
  模板上下文是缓存对象的元数据，另提供 `${sha1Path}` 占位符
  （sha1 前 4 位 + `/` + 其余部分的路径形式），配合 CDN 做内容寻址缓存：

```json5
cache-redirect : "https://cdn.example.com/${sha1Path}"
```

## 完整示例

日报接口：按天缓存，指纹防串数据。

```bash
echo 'o /data/report -match "{day:\"${http-qs-day}\"}" -l | jsonx @put "{day:\"${http-qs-day}\"}"' \
  > /home/demo/api/report/day
o /home/demo/api/report/day -e '
  cache-test:[{path:"/var/cache/report/day-${http-qs-day}.json",mime:"application/json",match:[{"day":"^2"}]}];
  cache-finger-key:finger;
  cache-duration:86400;
  http-output:json'
```

```bash
curl 'http://host/api/report/day?day=20261021'   # 第一次：执行命令并落缓存
curl 'http://host/api/report/day?day=20261021'   # 第二次：直接命中
```

## 动态校验语法

`cache-test.match`、`pvg-assert`、`preload.test`、`histest` 共用 Nutz `AutoMatch` 语法：

- `abc` — 等值
- `^正则` / `!^正则` — 正则匹配 / 取反
- `*` — 通配
- `~~abc` — 忽略大小写
- `1..100` — 数值区间
- `null` — 键不存在或值为 null
- `""` / `[BLANK]` — 空串
- `true` / `false` — 布尔

数组 = AND（全都要满足）；对象多键 = AND；想表达 OR 用 `pvg-assert` 的
`a|b` 或拆多条 `cache-test`。

## 注意事项

- 缓存文件就是普通对象，可以 `o` 查看、手动删除强制失效；
- `cache-duration` 以秒为单位，按缓存文件最后修改时间计算；
- 懒加载模式下，命令执行成功后自动写缓存——命令须把产出写到 stdout。

# 延伸阅读

- API 对象 — 全部 API 元数据
- 鉴权与历史 — 同样的匹配语法在权限断言中的应用
