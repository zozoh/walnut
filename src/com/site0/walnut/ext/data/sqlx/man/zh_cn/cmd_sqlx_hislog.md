# 过滤器简介

`@hislog` 指定了本批次 SQL 执行的历史记录方式

# 设计

在用户执行 `@exec` 的时候，通常是在修改数据，我们希望能把它的行为记录到一个历史操作表里。
这样每个用户对于数据的修改就能被追踪和查看。

通常来说，我们可以在 `~/.dao/default.dao.json` 增加一个配置项 `history` 指向一个配置。
本过滤器可以改变这个默认行为，可阻止本次 SQL 执行的历史记录行为，或者采用其他的配置。

无论怎样，这个历史记录的配置是重点，我们通过它可以指定:

1. 哪些 SQL 是需要记录的
2. 记录哪些上下文，记录成什么形式
3. 将历史记录存放到哪里

这些行为是配置在一个配置文件里的，下面是详细的说明:

```json
{
  /**
   * 根据全局上下文，生成一个对象，加入到 data explain 的上下文
   * 所谓全局上下文，参看 SqlxContext.getMergedInputAndPipeContext
   * 它融合了 Input 与 pipeContext，
   * 而 pipeContext 里默认包含了 "session" 信息。
   * > 这是通过在 sqlx 的初始化逻辑里调用 SqlxContext.setup 来加载的
   * 在准备这个上下文的时候，一定会生成一个 `__batch_no` 的的变量
   * 用来识别同一批历史记录
   */
  "assign": {
    "scene": "=scene",
    "session": "=session"
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
      // 譬如，如果 sqlName 是 "pet.insert"
      // 那么如果匹配成功，就会在上下文里添加
      //  sqlName = 'pet.insert'
      //  sqlName1 = 'pet'
      //  sqlName2 = 'inert'
      // 组织数据的时候可以直接使用这两个变量，当然如果你的正则表达式包含更多匹配组
      // 怎会生成更多的 sqlName${N} 变量
      "sqlName": "^(pet).(insert|update)$",
      //-----------------------------------------
      // 判断当前操作的数据行是否要记录一个历史
      // 采用的是 AutoMatch 语法
      // 默认的，如果不声明就表示
      //-----------------------------------------
      // 上下文就是操作记录本身
      // 返回 true 表示【通过】
      "testRecord": true,
      // 上下文就是 assign 段声明，全局上下文
      // 返回 true 表示【通过】
      "testContext": true,
      // 上下文就是操作记录本身
      // 返回 false 表示【通过】
      "ignoreRecord": false,
      // 上下文就是 assign 段声明，全局上下文
      // 返回 false 表示【通过】
      "ignoreContext": false,


      /**
       * 生成插入日志数据的模板对象，命令会根据下述上下文，对其
       * 进行 explainObj 操作
       * {
       *   sqlName: "pet.insert", // SQL 名称
       *   sqlName1: "pet",       // 第1个捕获组
       *   sqlName2: "insert",    // 第2个捕获组
       *   sqlName{N}: "???",     // 第N个捕获组
       *   session: {...},        // 会话对象
       *   domain: "demo",        // 当前域
       *   scene: "web"           // 来自 assign 段的设定
       *   item: {...}            // 当前处理对象
       * }
       */
      "data": {
        "scene": "=scene", // 在什么场景下?
        "user": "=session.unm", // 谁?
        "ip_addr": "=session.envs.CLIENT_IP",
        "ct": "=item.lm", // 在什么时候?
        "ref_tp": "=sqlName1", // 对什么数据?
        "ref_id": "=item.id",
        "action": "=sqlName2", // 做了什么操作?
        "detail": "->${item<json>}" // 具体的细节是什么?
      },
      /**
       * 有些字段， data 不方便设置，譬如，需要动态分配一个 ID 等
       * 这需要配置项约定更加丰富的语义。
       * 因此本配置段用来补充 data 里更多的字段。
       * > 这个与  `sqlx set` 子命令是一个道理。 它们的值的用法也是一样的
       * > 只不过这里的设置项目可以指定 "asDefault"，表示仅仅设置默认值，如果
       * > data 已经有了这个值了，那就不必设置了。譬如 数据的 `ct` 我们希望就采用
       * > 更新信息里的 `lm` 字段的值，但是有些数据表可能并未设置这个字段
       * > 因此我们可以指定，用当前时间作为 `ct` 字段的默认值
       * 具体每个 value 可以设置什么值，可以参看 `man sqlx set`
       */
      "setData": [
        {
          "name": "id",
          "value": "snowQ::10"
        },
        {
          "name": "ct",
          "value": "utc",
          "asDefault": true
        }
      ],
      // 将提取出来的数据暂存到【过滤管线上下文】
      "to": "HISTORY"
    }
  ],
  /**
   * 在整个 SQL 命令组执行的最后阶段，会通过 `target` 配置段
   * 声明数据具体如何记录到目标数据源中，可以支持多个输出目标
   */
  "target": [
    {
      /**
       * 从上下文管线的哪个变量里取值，取出来就是 data 声明的列表数据
       */
      "from": "HISTORY",
      /**
       * 目标数据源的名称，默认与当前上下文同一个数据源。
       */
      "dao": "hislog",
      /**
       * 执行什么插入语句模板
       */
      "sqlInsert": "his.insert"
    }
  ]
}
```

# 用法

```bash
sqlx @hislog
  # 【选】指定配置的 JSON 文件, 除非你显示的说明你不希望记录 hislog
  # 否则，它会默认尝试读取数据源配置的 "history" 指向的配置文件
  [-f /path/to/conf.json]
  # 开启这个选项，本次 SQL 的执行将不记录历史记录
  [-off]
```

# 示例

```bash
# 记录很多操作的历史记录
sqlx @hislog -f ~/history.json \
   @vars @exec ...
   @vars @exec ...
   @vars @exec ...
```
