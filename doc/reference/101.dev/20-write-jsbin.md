---
title: 编写 jsbin 业务脚本
module: 101.dev
keywords: [jsbin, jsc, 业务脚本, sys.exec, $log, $wn, AjaxResult, sqlx, datex, val]
source: [impl/box/cmd/jsc/jsc_walnut.js, impl/box/cmd/cmd_jsc.java]
related: [../40.commands/20-cmd-jsc.md]
---

# 编写 jsbin 概述

jsbin 是用 JavaScript 编写、在服务器（JVM）上运行的业务逻辑脚本，
由 `jsc` 命令执行。运行环境默认采用 nashorn 引擎，**仅支持 ES5 语法**，
不支持 ES6 的 `let`、模板字符串等特性。

```bash
# 执行 jsbin 脚本
jsc ~/path/to/my.js -vars '{param1:"hello", param2:"world"}'
```

引擎、内置对象等机制层面的内容见 [20-cmd-jsc.md](../40.commands/20-cmd-jsc.md)，
本篇只讲「脚本怎么写」。

## 脚本标准骨架

```js
// 《mybin_file_name.js》
//-----------------------------------
// 这里设置日志
var debug = debug || false;
var prefix = prefix || null;
$log.setup("mybin_file_name", debug, prefix);

// 分析脚本参数，这两个参数由调用者（jsc -vars）注入为全局变量
var id = id || null;
var age = age || 0;

function __main(id, age) {
  // 这里是脚本的主逻辑
}

// 最后调用主程序
var msBegin = Date.now();
try {
  $log.info("${mybin_file_name} begin with, id=%s, age=%s", id, age);
  __main(id, age);
} catch (err) {
  $log.printJsError(err);
  $log.error("!!${mybin_file_name} error happend: %s", err);
} finally {
  var ms_done = parseInt(Date.now() - msBegin);
  $log.info("${mybin_file_name} done in %sms, id=%s", ms_done, id);
}
```

要点：

- `$log.setup` 设置日志名与 debug 开关，日志会带 trace ID
- 入参一律 `var x = x || 默认值;` 读取，没传也不至于报错
- 主逻辑放 `__main`，外层包 try/catch/finally：异常进日志，结束记耗时

## 读取输入参数

`jsc -vars` 注入的参数，通过 ScriptEngine 的上下文设置为全局变量，
脚本直接读取：

```js
var id = id || null;
var age = age || 0;
```

## 脚本的输出契约

除非特殊说明，脚本对调用者（命令管道 / regapi）的输出应是合法的
`AjaxResult`：

```js
var scriptResult = {/*Any Object*/}; // or [{...}]

// 失败时：errCode 用于定位问题
$log.ajax_error("e.jsbin.mybin.XXX", "reason info");

// 成功时：data 是返回数据
$log.ajax_ok(scriptResult);
```

`$log.ajax_ok` / `$log.ajax_error` 会同时写入标准输出和日志，是推荐写法。
如果想完全手动控制，等价物是：

```js
sys.out.println(JSON.stringify({ok: true, errCode: null, data: scriptResult}));
```

## 服务器操作接口速览

脚本运行时注入这些接口对象（机制详见 [20-cmd-jsc.md](../40.commands/20-cmd-jsc.md)）：

- `sys` —— 执行命令、读写 stdout/err、会话信息（详解见
  [21-cmd-jsc_sys.md](../40.commands/21-cmd-jsc_sys.md)）
- `$log` —— 日志与 AjaxResult 输出（详解见
  [23-cmd-jsc_log.md](../40.commands/23-cmd-jsc_log.md)）
- `$wn` —— 助手函数（exec / execAsJson / genId / tryLock …，详解见
  [22-cmd-jsc_walnut.md](../40.commands/22-cmd-jsc_walnut.md)）
- `_` —— lodash

### sys：执行命令与 I/O

```js
//---------------------------------------------------
// 输出
//---------------------------------------------------
sys.out.println("hello");                        // > hello
sys.out.printlnf("hello %s", "world");           // > hello world
sys.err.println("something wrong");              // !> something wrong

// 打印当前账号与票据
sys.out.printlnf("I am %s, my Ticket is: %s",
                 sys.getAccountName(),
                 sys.getSessionTicket());

//---------------------------------------------------
// sys.exec 与 sys.exec2
//---------------------------------------------------
// 执行命令，输出直接写入脚本的标准输出
sys.exec("echo hello");

// 带标准输入执行命令
sys.exec("str | jsonx -cqn", '{say:"Hello"}');   // > {"say":"Hello"}

// 执行命令，输出作为字符串返回，供脚本继续处理
var re = sys.exec2("echo '{say:\"Hello\"}' | jsonx -qn");

// 支持 %s 占位符拼参数
var re = sys.exec2("echo '{say:\"%s\"}' | jsonx -qn", "Hello");

// 防止命令注入：去掉危险字符
sys.out.println(sys.safe("danger line;with 'rm *'"));
// > danger linewith rm *
```

### $log：日志输出

```js
$log.info("this is log: id=%s, age=%s", id, age);
$log.warn("this is warn: id=%s, age=%s", id, age);
$log.error("this is error: id=%s, age=%s", id, age);
```

### $wn：助手函数

```js
// 执行命令并返回输出；执行失败时主动抛 JS 异常
var re = $wn.exec("echo hello");               // re="hello"

// 执行命令，输出按 JSON 解析；失败抛异常
var re = $wn.execAsJson("echo '{say:\"hello\"}'");
// re={say:"hello"}
```

`$wn.exec` / `$wn.execAsJson` 相比 `sys.exec2` 多了一层「失败即异常」，
业务脚本里优先用它们。

### _：lodash

内嵌 lodash 函数集（4.17.15 完整版）。注意两个 nashorn 环境下的坑：

- **不要用 `_.assign` 合并对象**，用 `_.assignIn` 代替——`_.assign` 只拷
  贝自身可枚举属性，而 nashorn 里很多对象属性在原型链上，会静默拷贝出
  空对象
- **不要依赖 `_.isEmpty` 判断对象是否为空**，用 `_.keys(obj).length > 0`
  代替——它对 nashorn 对象的判断结果与浏览器不一致

原因与更多细节见
[20-cmd-jsc.md 的「lodash 的坑」一节](../40.commands/20-cmd-jsc.md)。

## 专门场景示例

### 访问 SQL 数据库（sqlx）

通过 `sqlx` 命令访问数据库。它的设计约束是：**不直接传 SQL 语句**，
只能传预定义的 SQL 模板名 + 变量上下文。一个实体 `pet`（表 `t_pet`）
通常配一组模板：`pet.select` / `pet.fetch` / `pet.count` /
`pet.insert` / `pet.update` / `pet.remove`。

模板的写法见 [11-write-sqlx-tmpl.md](11-write-sqlx-tmpl.md)，
这里只演示脚本侧怎么调。

#### 查询：sqlx @query

```js
var qs = JSON.stringify({
  columns: "id,title,msg_batch",   // 可选，默认 *
  filter: { msg_id: oMail.msg_id },// 查询条件 JSON，语法见下
  sorter: { ct: 1 },               // 1=ASC, -1=DESC
  pager: { pageSize: 20, pageNumber: 1 }  // 可选
});
var petList = $wn.execAsJson(
  "sqlx @vars @query pet.select @json -cqn", qs
);
// > [{...Pet1...},{...Pet2...}]
```

#### 查询条件 JSON 语法

`filter` 的完整语法（组合规则、AND/OR、操作符键、范围对象等）见
[11-write-sqlx-tmpl.md](11-write-sqlx-tmpl.md) 的「条件值语法」一节，
这里列最常用的速查：

```js
// 数字区间：[] 闭区间，() 开区间
{age: "[1,12]"}  // age>=1 AND age<=12
{age: "(1,12]"}  // age>1  AND age<=12
{age: "[1,)"}    // age>=1

// 枚举 → IN
{color: ["red","blue"]}  // color IN ('red','blue')

// 正则
{name: "^wang"}  // name REGEXP '^wang'

// 模糊：* 任意多字符，? 单字符
{name: "*A?C"}   // name LIKE '%A_C'

// 空值
{name: null}      // name IS NULL
{name: "<NULL>"}  // name IS NULL

// 取反：键名加 ! 前缀
{"!age": "[3,100)"}        // NOT (age>=3 AND age<100)
{"!color": ["red","blue"]} // NOT color IN (...)
```

#### @json 输出控制

`@json -cqn` 控制命令的 JSON 输出：

- `c` 紧凑不换行；`q` 键名加引号；`n` 不忽略 null
- `-as auto` 数组只有一个元素时拆开，直接返回元素本身
  （`pet.fetch` 查单条时配合它，省得处理单元素数组）

#### 增删改：sqlx @exec + @trans

```js
var CTX = {
  I: [ {/*..pet1..*/, ct:"%date:now", lm:"%date:now"},
       {/*..pet2..*/, ct:"%date:now", lm:"%date:now"} ],
  U: [ {/*..delta 含 id..*/, lm:"%date:now"} ],
  D: [ {id: "pet01"}, {id:"pet02"} ]
};
var re = sys.exec2([
      "sqlx @trans",
      "@vars =D -as list -reset @exec pet.remove",
      "@vars =I -as list -reset @exec pet.insert",
      "@vars =U -as list -reset @exec pet.update",
      "@json -cqn -as auto"
  ].join(" "),
  JSON.stringify(CTX)
);
```

要点：

- `@trans` 把整组 SQL 放在一个事务里；可用 `-level` 指定隔离级别
- `@vars =D -as list` 从上下文里取 `D` 键（列表用 `-as list`，
  单条 map 用 `-as map`，map 是默认值）；`-reset` 表示先清空上下文再设置
- 文本宏：`%date:now` 渲染为服务器当前时间字符串，`%ms:now` 渲染为毫秒时间戳
  （`-explain` 模式下展开）

### 访问 Walnut 标准对象

jsbin 里操作对象（WnObj）就是执行 `o` 命令（字段体系见
[01-std-obj.md](../10.concepts/01-std-obj.md)）：

```js
// 读元数据
var obj  = $wn.execAsJson("o ~/path/to/file.txt @json -cqn");
var obj  = $wn.execAsJson("o id:s2pqbka6j0huaq3bojm80c5dlu @json -cqn");
var objs = $wn.execAsJson("o @get id1 id2 @json -cqn");

// 查询
var qs = JSON.stringify({ nm: "^Z", age: "[20,]" });
var objs = $wn.execAsJson("o ~/tmp @query '" + qs + "' @json -cqn");
// 分页：-limit / -skip / -sort / -pager
var result = $wn.execAsJson(
  "o ~/tmp @query '" + qs + "' -limit 5 -skip 10 -sort '{ct:1}' -pager @json -cqn"
);
// > {list:[...], pager:{pageNumber, pageSize, pageCount, totalCount, count}}

// 内容读写
sys.exec('echo "hello world" > ~/path/to/hello.txt');
sys.exec("str > ~/path/to/hello.txt", "hello world");
var re = sys.exec2("cat ~/path/to/hello.txt");

// 删除：先查后删，输出被删对象
sys.exec("o ~/tmp @query 'nm:\"^X\"' @delete @json -cqn");
```

> `o` 命令的功能非常多，本篇只列脚本里最常用的。
> 完整手册见 `man o` 以及 40.commands 下的
> [cmd_o 专题](../40.commands/10-cmd-o-overview.md)。

### 获取唯一 ID

脚本里要新 ID（新对象主键、业务单号等）时，底层是 `val @gen` 命令
（完整模式语法见 [cmd_val](../../../src/com/site0/walnut/ext/data/val/man/zh_cn/cmd_val.md)）：

```js
//------------------------------------------------------------
// JS 里直接取值：优先用 $wn 封装，拿到的是字符串本身
//------------------------------------------------------------
var id  = $wn.genId();              // 等价于 val @gen snowQ::10，拿到字符串
var id2 = $wn.genId("symb::6");     // 短符号 ID，6 位随机
var ids = $wn.genIds(5);            // 一次生成 5 个，返回数组

//------------------------------------------------------------
// 也可以用 sys.exec2 自己拼，灵活但啰嗦
//------------------------------------------------------------
// 快速雪花 ID（36 进制毫秒时间戳 + 随机数）
sys.exec("val @gen snowQ::10");     // > mb1v610hb0sa9r4chp
sys.exec("val @gen snowQ::1");      // > mb1v6xff8
sys.exec("val @gen -n 3 snowQ::1"); // 一次 3 个

// UUID
sys.exec("val @gen UU32");

// 序列号：基于对象的字段计数，常用于业务单号
sys.exec("val @gen seq:M:4:~/path/to/yyyy-MM-dd#my_seq");
// > M0423  前缀 M + 4 位宽序列，滚动键为今日对象的 my_seq 字段
```

典型用法——给新记录补主键：

```js
var pet = {nm: nm, age: age, ct: "%date:now"};
pet.id = $wn.genId();               // 主键
pet.no = sys.exec2("val @gen seq:P:6:~/shop/yyyy-MM-dd#order_no").trim();
// > P000423 业务单号
```

### 处理日期

用 `datex` 命令（完整功能见
[cmd_datex](../../../src/com/site0/walnut/ext/sys/datex/man/zh_cn/cmd_datex.md)）：

```js
// 当前时间，常用：给记录打 ct/lm 时间戳
var now  = sys.exec2("datex @fmt 'yyyy-MM-dd HH:mm:ss' -n").trim();
// > 2026-10-29 10:25:31
var ms   = parseInt(sys.exec2("datex @fmt AMS -n").trim());  // 毫秒时间戳
var today = sys.exec2("datex @fmt yyyy-MM-dd -n").trim();   // 今天 0 点

// 相对推算
sys.exec2("datex @month -i 3");                 // 3 个月后
sys.exec2("datex @month -i -3");                // 3 个月前
sys.exec2("datex @day 10 @time 0");             // 本月 10 号 0 点
sys.exec2("datex @day -i -10");                 // 10 天前
sys.exec2("datex @day 1 @month -i 1 @day -i -1"); // 本月最后一天
sys.exec2("datex @set now+1d");                 // 1 天后
sys.exec2("datex @set 1694368557435");          // 毫秒时间戳转日期
sys.exec2("datex @fmt d MMM yy");               // 自定义格式
```

典型用法——拼装「最近 7 天」的查询条件：

```js
var dayBegin = sys.exec2(
  "datex @day -i -7 @time 0 @fmt 'yyyy-MM-dd HH:mm:ss' -n"
).trim();
var filter = JSON.stringify({ct: "[" + dayBegin + ",]"});
var list = $wn.execAsJson("sqlx @vars @query log.select @json -cqn", filter);
```

## 注意事项

1. 能用高级接口就用高级接口：能用 `$wn.execAsJson` 就不要
   `sys.exec2 + JSON.parse`
2. 引擎是 ES5：不要写 `let` / 模板字符串 / 箭头函数
3. 不要在 API 同步链路里跑长循环、大内存操作——投递给后台任务栈执行
4. JS 错误栈会进 `BOX` 日志，配合 `_TRACE_ID` 串联排查

# 延伸阅读

- [20-cmd-jsc.md](../40.commands/20-cmd-jsc.md) —— jsc 的机制：引擎、注入对象、命令参数、缓存
- [21-cmd-jsc_sys.md](../40.commands/21-cmd-jsc_sys.md) —— `sys` 对象：命令执行、对象读写、out/err/in
- [22-cmd-jsc_walnut.md](../40.commands/22-cmd-jsc_walnut.md) —— `$wn` 助手函数：execAsJson、genId、锁
- [23-cmd-jsc_log.md](../40.commands/23-cmd-jsc_log.md) —— `$log`：日志、trace ID、AjaxResult 契约
- [11-write-sqlx-tmpl.md](11-write-sqlx-tmpl.md) —— SQL 模板语法（sqlx 的作者侧规范）
- [10-cmd-o-overview.md](../40.commands/10-cmd-o-overview.md) —— cmd_o 专题：对象查询/读写/批量操作
- [20-write-app-init.md](20-write-app-init.md) —— 应用初始化模板（给新用户初始化目录骨架）
