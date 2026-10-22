---
title: 钩子机制（Hook）
module: 60.sched-async
keywords: [hook, hook_by, hook_user, ~/.hook, /sys/hook, 六类事件, JS 钩子]
source: [api/hook/, impl/hook/]
related: [mq, cron-pipeline, ../40.commands/sys-cmd.md]
---

# 钩子机制（Hook）

钩子是对象生命周期事件的自动响应：写文件时自动生成缩略图、删对象时同步清理关联数据。接口在 `api/hook/`，实现在 `impl/hook/`。

## 六类事件

`write | create | delete | meta | mount | move`——对象写入、创建、删除、元数据变更、挂载、移动时触发对应钩子。

## 钩子放哪里

两个位置（按动作分子目录）：

```text
/sys/hook/<action>/     全局钩子（root 维护，对所有域生效，先执行）
<主目录>/.hook/<action>/  用户钩子（root 用户是 /root/.hook/，其他是 /home/<域>/.hook/）
```

查找顺序：**全局先、用户后**；目录内按文件名（`nm`）升序执行；隐藏文件忽略。钩子目录本身被 `st`（syncTime）监控，文件改动即刻生效（带缓存）。

系统自带示例（`WnSetup` 初始化）：`/sys/hook/write/create_thumbnail`，`hook_by` 匹配 `mime:^image/` 的图片写入后自动生成缩略图。

## 钩子文件格式

钩子文件可以是**命令模板**或 **JS 文件**（`.js` 后缀，由 `JsCommandHook` 执行）：

```bash
mkdir -p /sys/hook/write
# 命令模板钩子：内容当命令模板渲染执行，占位符=对象元数据
echo 'imagic ${id} -filter "cover(200,200)" -out ${ph_200}' \
  > /sys/hook/write/gen_200
chmod +x /sys/hook/write/gen_200
o /sys/hook/write/gen_200 -e 'hook_by:[{mime:"^image/"}]'

# JS 钩子：全局 obj 变量是当前对象，sys.exec 执行命令
echo 'sys.exec("obj "+obj.id()+" -e \"thumb:done\"")' \
  > /sys/hook/write/mark_done.js
```

## 元数据

- **`hook_by`**：匹配条件数组（全部满足才触发），条件语法同 [缓存匹配](../50.regapi/cache.md#动态校验语法automatch)：等值、`^正则`、`!` 取反、`*` 通配、`a..b` 区间、`null` 存在性等
- **`hook_user`**：以谁的身份执行（**仅全局钩子有效**；用户钩子强制以对象属主身份执行）

## 调试

用 `hookx` 命令离线查看/触发钩子（见 [系统管理命令](../40.commands/sys-cmd.md)）：

```bash
# 看某个对象会触发哪些钩子（不执行）
hookx @get ~/photo/a.jpg @keys 'thumb' @view

# 模拟触发
hookx @get ~/photo/a.jpg @invoke write
```

## 注意事项

- 钩子目录里的文件操作**不会**再触发钩子（防递归）；
- 钩子执行失败会打断原操作——写钩子脚本要健壮，必要时装 try/catch；
- `hook_by` 尽量收窄条件，全局钩子尤其要谨慎（所有域的图片写入都会过一遍）。

## 延伸阅读

- [定时任务](cron-pipeline.md) — 钩子里跑长任务要投递给 task/mq
- [消息队列](mq.md)
