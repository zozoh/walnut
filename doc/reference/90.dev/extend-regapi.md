---
title: 实战：开发完整 API
module: 90.dev
keywords: [regapi 实战, API 示例, http-output, 模板, jsonx, 上传, 缓存]
source: [ext/sys/httpapi/]
related: [../50.regapi/10-api-object.md, ../50.regapi/20-request-object.md, ../50.regapi/40-cache.md, jsexec-api]
---

# 实战：开发完整 API

把前几章的机制串成三个可落地的 API。约定：API 都在 `/api/<域>/` 下，命令为文件内容。

## 例 1：列表接口（GET + 缓存）

```bash
mkdir -p /home/demo/api/news
cat > /home/demo/api/news/list <<'EOF'
o /home/demo/data/news -match '{race:"FILE", live:1}' -sort 'lm:1' -limit '${http-qs-limit?20}' -l \
  | jsonx @pick 'id,nm,lm' -cqn
EOF
chmod +x /home/demo/api/news/list
o /home/demo/api/news/list -e '
  http-output:json;
  cache-test:[{path:"/var/cache/news-list-${params.tp?all}.json",mime:"application/json"}];
  cache-duration:300;
  api-param-name:tp'
```

要点：

- `http-output:json` 响应按 JSON 返回；
- `cache-test` 把结果缓存 5 分钟（见 API 缓存）；
- 模板占位符支持默认值：`${http-qs-limit?20}`（缺失时用 20），还可 `${key?@otherKey}` 取备用键。

## 例 2：创建接口（POST + JSON body）

```bash
cat > /home/demo/api/news/create <<'EOF'
# http-body:json 时 body 字段已并入参数表
o /home/demo/data/news -c \
  -e 'race:"FILE", title:"${http-qs-title}", tp:"news", live:1'
echo '{"ok":true,"id":"${id}"}'
EOF
chmod +x /home/demo/api/news/create
o /home/demo/api/news/create -e '
  http-output:json;
  http-body:json;
  http-method:POST;
  api-access-token:tk_news'
```

调用：

```bash
curl -X POST 'https://host/api/news/create?tk_news=xxx&title=大新闻&body={"content":"..."}'
```

要点：`http-method:POST` 限定方法；`api-access-token` 加令牌；对象元数据里的 `${id}` 是刚创建对象的 ID。

## 例 3：上传接口（multipart）

```bash
cat > /home/demo/api/avatar/upload <<'EOF'
cp id:${http-qs-file} ~/avatars/${http-www-me-id}.png
imagic ~/avatars/${http-www-me-id}.png -filter 'cover(200,200)' -out ~self~
echo '{"ok":true}'
EOF
chmod +x /home/demo/api/avatar/upload
o /home/demo/api/avatar/upload -e '
  http-output:json;
  http-body:form;
  http-www-home:/home/demo/website'
```

要点：`http-body:form` 时文件字段变 `id:xxx` 临时对象（见 请求对象）；站点会话注入 `http-www-me-id`。

## 例 4：第三方聚合（模板渲染响应）

```bash
cat > /home/demo/api/weather <<'EOF'
xapi send openweather default now -vars '{city:"${params.city}"}' \
  | tmpl ~/tmpl/weather.txt
EOF
chmod +x /home/demo/api/weather
o /home/demo/api/weather -e 'api-param-name:city'
```

`~/tmpl/weather.txt` 用 `tmpl` 语法渲染（`${}` 占位、`${#loop}` 循环、`${#if}` 分支，见 `man tmpl`）。

## 设计检查清单

- [ ] 只读接口考虑 `cache-*`；需要鉴权则缓存自动失效（互斥，源码保证）；
- [ ] 写接口限定 `http-method`；
- [ ] 对外接口加 `api-access-token` 或站点鉴权；
- [ ] 上传接口注意 `http-tmp-duraion`，及时 `cp` 走临时文件；
- [ ] 返回统一 `{ok:..., data:...}` 结构，前端好处理。

## 延伸阅读

- API 对象 / 请求对象 / 缓存
- JS 执行环境 — 复杂逻辑换 JS 写
- xapi
