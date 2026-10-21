---
title: API 缓存（cache-*）
module: 50.regapi
keywords: [cache-test, cache-duration, cache-finger-key, cache-lazy, cache-redirect, 302, 缓存]
source: [ext/sys/httpapi/HttpApiModule.java]
related: [api-object, path-params, auth-history]
---

# API 缓存（cache-*）

regapi 自带页面级缓存：把命令的产出物缓存成文件对象，后续相同请求直接命中，跳过命令执行。适合「读多写少」的接口（报表、列表页、静态化内容）。

## 工作原理

1. 请求到达后先执行 `cache-test` 匹配；
2. 命中且未过期 → 直接输出缓存对象（或 302 重定向）；
3. 未命中 → 正常执行命令，执行完把产出写到缓存路径（懒加载模式 `cache-lazy`，默认开），下一次请求命中。

**以下请求无视缓存**（源码硬规则）：

- 非 GET 请求（POST/PUT/...）；
- 需要站点鉴权的请求（`http-www-auth`）；
- 声明了 `http-dynamic-header` 的 API。

## 元数据全集

| 元数据 | 类型 | 说明 |
|---|---|---|
| `cache-test` | JSON 数组 | 匹配条件列表，见下 |
| `cache-duration` | int（秒） | 缓存有效期；0 或缺省 = 永不过期 |
| `cache-finger-key` | string | 请求指纹键名（见下） |
| `cache-finger-as` | string | 指纹算法，默认 `MD5` |
| `cache-lazy` | bool | 懒加载，默认 `true` |
| `cache-redirect` | string | 命中时 302 到该 URL 模板 |

### cache-test

数组，每项一个 NutMap，**逐项尝试，先中先得**：

```js
{
  cache-test : [
    { path : "/var/cache/report/today.json",
      mime : "application/json",
      match : [ { "qs.day" : "today" } ] },
    { path : "/var/cache/report/${params.uid}.json",
      mime : "application/json",
      match : { "qs.tp" : ["hot","new"] } }
  ]
}
```

- `path`：缓存文件路径模板，支持 `${xxx}` 占位（从请求元数据取值，如 `${params.uid}`）；
- `mime`：命中时的响应 Content-Type；
- `match`：匹配条件（数组为 AND，见 [动态校验](#动态校验语法)）；缺省总是匹配。

### 指纹：cache-finger-key

有些接口同一路径会因参数不同产出不同内容（如 `?day=20261020` 与 `?day=20261021`）。设置 `cache-finger-key` 后，系统会对**请求查询串**计算摘要（默认 MD5），并写入缓存对象的该键。后续请求摘要不一致即视为缓存失效——相当于把参数纳入缓存校验。

### 输出方式

- 默认：把缓存对象内容写入响应流；
- 设置 `cache-redirect` 时：`302` 到模板渲染的 URL，模板可用 `${sha1Path}`（对象 SHA1 分两段的路径形式），配合前置静态服务/CDN 做内容寻址缓存：

```js
cache-redirect : "https://cdn.example.com/${sha1Path}"
```

## 完整示例

```bash
# 日报接口：按天缓存，指纹防串数据
echo 'obj /data/report -match "{day:\"${http-qs-day}\"}" -l | jsonx @put "{day:\"${http-qs-day}\"}"' \
  > /home/demo/api/report/day
chmod +x /home/demo/api/report/day
o /home/demo/api/report/day -e '
  http-api:true;
  cache-test:[{path:"/var/cache/report/day-${http-qs-day}.json",mime:"application/json",match:[{"qs.day":"^2"}]}];
  cache-finger-key:finger;
  cache-duration:86400;
  http-output:json'
```

```bash
curl 'http://host/api/report/day?day=20261021'   # 第一次：执行命令并落缓存
curl 'http://host/api/report/day?day=20261021'   # 第二次：直接命中
```

## 动态校验语法

`cache-test.match` 与 `pvg-assert`、`preload` 等共用同一套匹配语法（`WnMatch` 体系）：

- `^正则` — 正则匹配；
- `!xxx` / `!^正则` — 取反；
- `*` — 通配；
- `~~` 前缀 — 忽略大小写；
- `a..b` — 数值区间；
- `null` / `""` / `[BLANK]` — 存在性/空值判断；
- `true/false` — 布尔。

## 注意事项

- 缓存文件就是普通对象，可以 `obj` 查看、手动删除强制失效；
- `cache-duration` 以秒为单位，按缓存文件最后修改时间计算；
- 懒加载模式下，命令执行成功后自动写缓存——命令须把产出写到 stdout（`http-output` 正常输出）。

## 延伸阅读

- [API 对象](api-object.md) — 全部 API 元数据
- [鉴权与历史](auth-history.md) — 同样的匹配语法在权限断言中的应用
