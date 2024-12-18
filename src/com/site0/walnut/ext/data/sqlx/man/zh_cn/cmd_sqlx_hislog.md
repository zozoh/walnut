# 过滤器简介

`@hislog` 指定了本批次 SQL 执行的历史记录方式

# 设计

我们希望通过少量的配置，可以在用户执行 `@exec` 的时候悄悄的向某个历史记录表
记录信息，这个表默认的是与当前处于相同的数据源，当然你也可以指定一个特殊的数据源。

我们通过这个过滤器，我们可以指定:

1. 哪些 SQL 是需要记录的
2. 记录哪些上下文，记录成什么形式
3. 还有没有更多需要记录的信息

这些行为是配置在一个配置文件里的:

```json
{
  /**
   * 可以指定一个不同的数据源，如果不设置，则会采用当前数据源
   */
  "daoName": null,
  /**
   * 根据全局上下文，生成一个对象，加入到 data explain 的上下文
   */
  "assign": {
    "scene": "=scene"
  },
  /**
   * 每次 @exec 将逐个执行下面的配置项, 对应 -list 的上下文
   * 会循环每个列表元素，根据配置提取数据对象，暂存到上下文的某个
   * 列表变量里
   */
  "logs": [
    {
      // 看看执行的 sql 是否匹配，如果匹配了
      // 则会向上下文中添加匹配组 sqlName1, sqlName2 ...
      "sqlName": "^(pet).(insert|update)$",
      // 判断本行数据是否要执行插入
      // 如果不声明，就表示每次执行都匹配
      // 采用的是 AutoMatch 语法
      "test": true,
      /**
       * 一个 explain 对象，上下文就是
       * {
       *   sqlName: "pet.insert", // SQL 名称
       *   sqlName1: "pet",       // 第1个捕获组
       *   sqlName2: "insert",    // 第2个捕获组
       *   session: {...},    // 会话对象
       *   domain: "demo",    // 当前域
       *   scene: "web"       // 来自 assign 段的设定
       *   item: {...}        // 当前处理对象
       * }
       */
      "data": {
        "scene": "=scene",          // 在什么场景下?
        "user": "=session.unm",     // 谁?
        "ct": "=vars.ct",           // 在什么时候?
        "ref_tp": "=sqlName1",      // 对什么数据?
        "ref_id": "=item.id",
        "action": "=sqlName2",      // 做了什么操作?
        "detail": "->${item<json>}"   // 具体的细节是什么?
      },
      // 将提取出来的数据暂存到【过滤管线上下文】
      "to": "HISTORY"
    }
  ]
}
```

# 用法

```bash
sqlx @hislog
  [-f /path/to/conf.json]    # 指定配置的 JSON 文件
```

# 示例

```bash
sqlx @hislog -f ~/history.json @vars @exec ...
```
