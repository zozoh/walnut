# 过滤器简介

`@decorate` 为各个节点或者边执行动作时添加默认参数。

在 `@process` 之前，可以添加任意多的  `@decorate`，它们最终
会按照限后顺序组合成一个大的默认参数判断列表

```json5
[
  {
    "test": {
      "type": "jsc",
      "path": "*wf_task_create.js",
      "params": {
        "name": "T500"
      }
    },
    "params": {
      "eventId": "=EVENT.id",
      "prevId": "=TASK.id",
      "nodeName": "=NEXT_NAME"
    }
  },
  ...
]
```

# 用法

```bash
wf ... @decorate
 [InputJson]              # 【选1】参数是直接的 JSON 内容，如果并未声明，
                      # 并且也没有 `-f`，那么就尝试从标准输入读取
 [-mode all|one]      # 【选】执行模式
                      #  - one  遇到第一个满足的条件，就执行合并，并且退出循环
                      #  - all  对所有条件都判断，只要满足，就进行合并
                      # 默认 "one"
 [-f /path/to.json]   # 【选1】如果条件过于复杂，可以放到一个文件的内容里
```

# 示例

```bash
$demo> wf ~/demo.json @var -f ~/abc.json @decorate -f ~/de.json @process
```
