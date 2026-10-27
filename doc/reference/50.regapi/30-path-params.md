---
title: 路径参数与动态路由（_ANY）
module: 50.regapi
keywords: [_ANY, api-param-name, args, params, 通配, 动态路由]
source: [ext/sys/httpapi/HttpApiModule.java]
related: [10-api-object, 20-request-object, 40-cache]
---

# 路径参数与动态路由（_ANY） 概述
`/api/user/123/order/456` 这样的 REST 路径，树上显然不可能为每个 ID 建一个文件。
怎么办？

regapi 的路由就是**文件系统路径匹配**：找不到精确文件时，Walnut 会逐层尝试
名为 `_ANY` 的通配对象——这就是动态路由的全部机制。

匹配逻辑（源码 `HttpApiModule` 的路径解析循环）：

1. 按路径段逐层 `fetch`；
2. 精确命中 → 继续下一层；
3. 未命中 → 尝试当前目录下的 `_ANY`：
   - **`_ANY` 是目录**：吃掉这一段路径，记入 `args`，并继续往下一层匹配；
   - **`_ANY` 是文件**：吃掉剩余所有路径段（以 `/` 连接），匹配结束。

## 两种形态

### 1. 目录 `_ANY` + `_action` 子文件

目录型 `_ANY` 自己不执行，而是执行它下面的 `_action` 文件（如果还有剩余路径段，则继续往下层匹配）：

```bash
mkdir -p /home/demo/api/user/_ANY/order/_ANY
echo 'echo "user=${args[0]} order=${args[1]}"' > /home/demo/api/user/_ANY/order/_ANY/_action
chmod +x /home/demo/api/user/_ANY/order/_ANY/_action
```

```
GET /api/user/zozoh/order/W20261021
-> user=zozoh order=W20261021
```

### 2. 文件 `_ANY`（`*` 语义）

文件型 `_ANY` 捕获剩余全部路径：

```bash
o /home/demo/api/files/_ANY -e 'api-param-name:ph'
echo 'cat /home/demo/files/${params.ph}' > /home/demo/api/files/_ANY
# 捕获 /api/files/a/b/c.txt，输出 /home/demo/files/a/b/c.txt 的内容
```

## 命名参数：api-param-name

通配段默认进入 `args` 数组，还可以在 `_ANY` 对象上设置 `api-param-name` 给它起名字，它会同时进入 `params` 映射：

```bash
o /home/demo/api/user/_ANY -e 'api-param-name:uid'
# 请求 /api/user/42 -> params = {uid:"42"}，args = ["42"]
```

命令/JS 中的读取方式：

```bash
echo '{"uid":"${params.uid}"}'        # params 是 Map
echo "args0=${args[0]}"               # args 是数组（JS 里也支持 args[0]）
```

这两个键（`args`、`params`）在执行前被注入请求元数据（见 请求对象）。

## 404 规则

- 路径中某一层既没有精确对象也没有 `_ANY` → `e.api.nofound`（404）；
- 目录型 `_ANY` 下没有 `_action` 子文件 → `e.io.obj.noexists`（同样 404）；
- `http-api` 不是 API 对象的声明属性——它是**请求对象**上的元数据（记录本次命中的
  API 对象名，见 20-request-object）；服务端也不校验可执行位——`.regapi/api/` 树下
  凡是被路由命中的对象，都会被执行。

## 典型场景

- **REST 风格接口**：`/api/user/_ANY` + `api-param-name:uid`；
- **文件直出**：`/api/files/_ANY` 捕获任意深路径后 `cat`；
- **版本路由**：`/api/v1/_ANY` 与 `/api/v2/_ANY` 并存。

## 注意事项

- `_ANY` 必须全大写；
- 目录 `_ANY` 依赖 `_action` 子文件执行，别忘了建；
- `api-param-name` 设置在 `_ANY` 对象自身上，而不是 `_action` 上。

# 延伸阅读

- API 对象
- 请求对象 — args / params 注入位置
- 一切皆对象
