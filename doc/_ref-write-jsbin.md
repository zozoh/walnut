# 如何构建 jsbin

## 什么是 jsbin

用 Javascript 编写的在服务器(Jvm 虚拟机)上运行的业务逻辑脚本，运行环境默认采用 `nashorn` 引擎。
仅支持 ES5 语法规范，不支持 ES6 的 let、字符串模板等特性。

```bash
# Walnut 服务器的 jcs 命令可以用来执行 jsbin 脚本
jsc ~/path/to/my.js -vars '{param1:"hello", param2:"world"}'
```

## 一个 jsbin 脚本的基本结构

```js
// 《mybin_file_name.js》
//-----------------------------------
// 这里设置日志
var debug = debug || false;
var prefix = prefix || null;
$log.setup("mybin_file_name", debug, prefix);

// 分析脚本参数，这两参数会由调用者，也就是 jsc 通过 -vars 参数指定
// 通过 Java 的 ScriptEngine 的上下文设置进来，可以直接读取
var id = id || null;
var age = age || 0;

function __main(id, age) {
  // 这里是脚本的主逻辑
  // 可以通过脚本全局变量
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

## 如何读取输入参数

```js
// 通过 jsc -vars 注入的参数，脚本直接当作全局变量读取
var id = id || null;
var age = age || 0;
```

## 脚本的输出

除非特殊说明，脚本的输出通常是合法的 `AjaxResult`，它的结构如下:

```js
var scriptResult = {
  /*Any Object*/
}; // or [{...}]
var re = {
  // 表示操作是否成功，true 表示成功，false 表示失败
  ok: true,
  // 如果 ok=false, 这里是错误代码，用于定位问题
  errCode: "e.jsbin.${mybin_file_name}.${Error Code}",
  // ok=true 时就是返回数据
  // ok=false 时就是 reason 信息
  data: scriptResult,
};
// 仅仅写入到标准输出
sys.out.println(JSON.stringify(re));

// 【推荐】采用高级方法将错误同时写入标准输出和日志
$log.ajax_error("e.jsbin.${mybin_file_name}.${Error Code}", "reason info");

// 【推荐】采用高级方法将返回值同时写入标准输出和日志
$log.ajax_ok(scriptResult);
```

## 服务器操作接口

脚本运行时，会被注入以下接口对象，方便脚本获得更多外部能力:

- `sys` : 访问服务器系统的接口
- `$log` : 日志输出高级接口
- `$wn` : 服务器帮助函数接口
- `_` : 这里还内嵌了一个 lodash 函数集

### 使用 `sys` 访问服务器资源

- `sys.exec` 执行命令，并将其输出写入到脚本的标准输出
- `sys.exec2` 执行命令，并将其输出返回为一个字符串，以便脚本后续处理
- `sys.safe` 防止命令注入的帮助函数
- `sys.out` 标准输出接口
- `sys.err` 错误输出接口
- `sys.getSessionTicket` 获取当前 Session 的票据
- `sys.getAccountName` 获取当前会话账号名称

```js
//---------------------------------------------------
// 输出信息
//---------------------------------------------------
// 直接向脚本标准输出写入 hello
sys.out.println("hello"); // > hello
sys.out.printlnf("hello %s", "world"); // > hello world
// 向脚本错误输出写入 'something wrong'
sys.err.println("something wrong"); // !> something wrong
sys.err.printlnf("something:[%s] wrong", "Unknown"); // !> something:[Unknown] wrong
// 打印当前账号信息
sys.out.printlnf(
  "I am %s, my Ticket is: %s",
  sys.getAccountName(),
  sys.getSessionTicket()
);
// > I am demo, my Ticket is: pemvb5uvjmiaromo7q53s2o1b6
//---------------------------------------------------
// sys.exec 与 exec2
//---------------------------------------------------
// 通过命令向脚本标准输出写入 hello
sys.exec("echo hello"); // > hello

// 接受'{say:"Hello"}'为标准输入，执行命令组合并写入到标准输出
sys.exec("str | jsonx -cqn", '{say:"Hello"}'); // > {"say":"Hello"}

// 执行命令，并将其输出返回为一个字符串
var re = sys.exec2("echo '{say:\"Hello\"}' | jsonx -qn");
sys.out.println(re);
// >>>
// {
//    "say": "Hello"
// }

// 执行命令，标准输入为'Hello'，并将其输出返回为一个字符串
var re = sys.exec2("echo '{say:\"%s\"}' | jsonx -qn", "Hello");
sys.out.println(re);
// >>>
// {
//    "say": "Hello"
// }

// 防止命令注入
sys.out.println(sys.safe("danger line;with 'rm *'"));
// > danger linewith rm *
```

### 使用 `$log` 输出日志

```js
$log.info("this is log: id=%s, age=%s", id, age);
$log.warn("this is warn: id=%s, age=%s", id, age);
$log.error("this is error: id=%s, age=%s", id, age);
```

### 使用 `$wn` 的帮助方法

```js
// 执行命令，并返回命令执行结果，如果执行失败，则主动抛 JS 异常
var re = $wn.exec("echo hello");
// > re="hello"

$wn.exec("echo 'bad json' | jsonx");
// > 异常:
// javax.script.ScriptException : javax.script.ScriptException: Error: e.json.InvalidFormat :
// !Json syntax error nearby [row:1,col:1 char 'b'], reason: 'Unexpect String = bad json
// ' in file:.../jsc_walnut.js at line number 74 at column number 15

// 执行命令，并将返回命令执行结果用 JSON 解析，如果执行失败，则主动抛 JS 异常
var re = $wn.execAsJson("echo '{say:\"hello\"}'");
// re={say:"hello"}

$wn.execAsJson("echo 'bad json'"); //命令虽然执行成功，但是 JSON parse 会异常
// > 异常:
// > javax.script.ScriptException : javax.script.ScriptException:
// SyntaxError: Invalid JSON: <json>:1:0 Expected json literal but found b
//
// bad json
// ^ in file:...jsc_walnut.js at line number 80
```

### 使用 `_` 的帮助方法

使用 `lodash` 需要注意，不要用 `_.assign` 来合并对象，因为JS的引擎问题。
你需要采用 `_.assignIn` 来代替

## 专门场景示例

### 访问 SQL 数据库

通过 `sqlx` 命令访问数据库， 这个命令设计思路为:

1. 为了安全，不能直接输入给命令 SQL 语句，只能传递给它一个 SQL 模板名称
2. 模板支持比较复杂的语法： 判断，循环的多层嵌套组合，根据上下文可以生成完全不同的 SQL
3. 为了支持模板的渲染，需要给命令传递一个上下文集合，你可以认为它就是一个大的 `HashMap`
4. 一个 `sqlx` 命令声明周期，支持事务，执行多条 SQL
5. 如果一个表 `t_pet`，我们称`pet`为【实体】，为此我们会建立一组模板:
   - `pet.select` 查询多条: 支持 columns,filter,sorter,pager 定制
   - `pet.fetch` 获取单条，通过 id 或者 UNIQUE 字段
   - `pet.count` 统计数量, 与 select 共用一个 filter
   - `pet.insert` 单条或批量插入
   - `pet.update` 单条或批量更新
   - `pet.remove` 单条或批量删除

#### 查询: `sqlx @query`

```js
var qs = (qs = JSON.stringify({
  columns: "id,title,msg_batch,edi_msg_count", // 可选, 默认就是 *
  filter: { msg_id: oMail.msg_id }, // 用 JSON 表达条件，这个后面会详细解释它的语法
  sorter: { ct: 1 }, // [通常可选] 相当于 ct ASC； 1=ASC, -1=DESC
  pager: { pageSize: 20, pageNumber: 1 }, // [通常可选]
}));
// 通过 Walnut 命令 sqlx 的 @vars 接受到 qs 的 JSON 变量，然后执行
// 预定义 SQL pet.select
var petList = $wn.execAsJson("sqlx @vars @query pet.select @json -cqn ", qs);
// > [{...Pet1...},{...Pet2...},{...Pet3...}]
```

#### 查询条件 JSON 语法

```js
// 查询数字范围
{age: "[1,12]"}  // age>=1 AND age<=12
{age: "(1,12]"}  // age>1 AND age<=12
{age: "[1,12)"}  // age>=1 AND age<12
{age: "(,12]"}   // age<=12
{age: "(,12)"}   // age<12
{age: "[1,)"}    // age>=1
{age: "(1,)"}    // age>1

// 查询枚举值
{color: ["red","blue"]}  // color IN ('red', 'blue')

// 正则表达式
{name: "^wang"}  // name REGEXP "^wang"

// 模糊匹配
{name:"*A?C"}  // name LIKE '%A_C'

// 空值
{name:null}      // name IS NULL
{name:"<NULL>"}  // name IS NULL

// 取反
{"!age":"[3,100)"}          // NOT (age>=3 AND age<100)
{"!color": ["red","blue"]}  // NOT color IN ('red', 'blue')
{"!name": "^wang"}          // NOT name REGEXP "^wang"
{"!name":"*A?C"}            // NOT name LIKE '%A_C'
{"!name":null}              // NOT name IS NULL
{"!name":"<NULL>"}          // NOT name IS NULL
```

#### `@json` 子命令

`@json -cqn` 是对命令输出 JSON 的控制:

- `c`:紧凑不换行
- `q`:引号包裹名称
- `n`:不要忽略 null 值
- `-as auto` 表示如果这是一个数组且只有一个元素，那么就拆开数组仅返回元素。通常用在 fetch 单条数据这很有用，毕竟你返回一个只有一个元素的数组，对于调用者来说，多少有点滑稽

#### 增删改: `sqlx @exec`

```js
// 准备要操作的变量
var CTX = {
  I: [
    {/*..pet1..*/, ct:"%date:now", lm:"%date:now"},
    {/*...pet2..*/, ct:"%date:now", lm:"%date:now"}
  ],
  U: [
    {/*..delta1..*/, lm:"%date:now"},  //  通常 delta 要包含 id 这个主键以便更新
    {/*..delta1..*/, lm:"%date:now"}
  ],
  D: [{id: "pet01"}, {id:"pet02"}]
}
// 在命令里使用它们
re = sys.exec2([
      "sqlx @trans",
      "@vars =D -as list -reset @exec -explain pet.remove",
      "@vars =I -as list -reset @exec -explain pet.insert",
      "@vars =U -as list -reset @exec -explain pet.update ",
      "@json -cqn -as auto"
  ].join(" "),
  JSON.stringify(CTX)
);
```

`@vars` 用来为命令上下文设置数据变量, `=D` 表示从脚本输入变量集合 `CTX` 里面获取 `D` 这键下的子集合，按照上面的例子，它是一个数组。因此需要通过`-as list` 指明这个输入是一个数据元素的列表。 当然如果仅仅操作一个记录时，它通常是个 `Map`，因此就需要指定 `-as map`。这也是这个子命令的默认值。 接着 `-reset` 表示设置前需要清空上下文变量，否则会将新内容加入上下文集合。

`@exec` 子命令输出的信息是一个 `SqlExecResult` 对象，它的格式为
`{updateCount:1, batchTotal:1}` 当然它的格式也被 `@json` 最终控制。
它的 `-explain` 选项表示需要在执行 SQL 之前展开上下文变量的文本宏。

文本宏是另外一个概念，我经常用到的就是 `"%date:now`，在 `-explain` 模式下程序发现特殊文本标记 `%date:now` 就会用服务当前的时间代替这个值，即类似 `'2025=05-23 13:23:23.875'` 这样的标准时间戳字符串。这样客户端在更新或者插入数据时，只需要设置 `{ct:"%date:now"}` 变量，就能得到服务器执行时刻的时间戳了。

同理，你可能还需要用到的一个文本宏，是 `%ms:now`，它渲染出来的是一个服务器时间戳的长整数版本。

#### 事务: `sqlx @trans`

你可能注意到，我们执行 `sqlx` 通常会增加 `@trans` 这个子命令，它的作用是将一组 SQL 语句作为一个事务执行，以便保证数据的一致性。通常我们会在增删改操作中使用它。它采用的是数据库默认的事务级别

如果你想指定事务级别，可以 `sqlx @trans -level 1`

-  `1` - TRANSACTION_READ_UNCOMMITTED
-  `2` - TRANSACTION_READ_COMMITTED
-  `4` - TRANSACTION_REPEATABLE_READ
-  `8` - TRANSACTION_SERIALIZABLE
- `-1` - 【默认】采用数据库默认事务级别

### 访问 Walnut 标准对象

Walnut 标准对象(以后我们称为 `WnObj`)，是 Walnut 服务器标准存储结构的
基本操作单位。它有两种类型(我们称为族类):

1. `DIR` 目录对象，它可以包含子对象，但不能有二进制内容
2. `FILE` 文件对象，它不能包含子对象，但是可以有二进制内容

与 Linux 类似，在用户的主目录 `/home/${My Name}/` 下会存放这个用户
所有数据对象，我们称主目录 `/home/${My Name}/` 为**用户域**或者**域**。

一个标准对象，逻辑上有下面这些字段:

```bash
# 标准信息
id: "abc123xyz..."       # VARCHAR(32): 唯一ID: UU32
ct: '2025-03...'         # DATETIME(3): 创建时间（毫秒级）
lm: '2025-03...'         # DATETIME(3): 最后修改时间（毫秒级）
c: "xiaobai"             # VARCHAR(50): 创建者ID
m: "xiaobai"             # VARCHAR(50): 修改者ID
g: "bunya"               # VARCHAR(50): 所属组

# 对象基本信息
nm: "document.pdf"       # VARCHAR(256): 文件名或目录名
title: "Document Title"  # VARCHAR(512): 文件标题或描述
lbls: ["Tag1"...]        # VARCHAR(256): 标签列表
race: "FILE"             # CHAR(5): 对象族类 (FILE/DIR)
tp: "pdf"                # VARCHAR(20): 文件后缀

# DIR 对象专有信息
mnt: "file:///..."       # VARCHAR(512): 挂载信息

# FILE 对象专有信息
mime: "application/pdf"  # VARCHAR(128): MIME类型
len: 102400              # INT: 文件大小（字节）
sha1: "da39a3ee5e..."    # VARCHAR(40): 文件内容的SHA1哈希值
local: "{...}"           # VARCHAR(512): 本地上传文件信息

# 图片文件专有信息
width: 0                 # INT: 图片宽度
height: 0                # INT: 图片高度
thumb: "id:xxx"          # VARCHAR(128): 图片缩略图文件

# 扩展信息，这里可以指定更多的特殊字段
# 根据具体的业务需求，一个对象可以被当作任何事物
# 一条评论，一张订单，一篇文章，一个3D 模型均可
```

虽然现阶段，我们的对象根存储介质是 MongoDB，但我们并不会假设`WnObj`实际的存储方式。
因为我们可以实时的通过 `mnt` 将任何一个目录挂载到任意存储介质，譬如：

- 某个 RDS 数据表
- 某个 Redis 集合
- 某个 MongoDB 集合
- 某个本地文件目录
- 甚至是本机内存

同时，对于文件对象的内容，我们也可以通过`mnt`，让文件内容存放到

- 本地文件，
- Redis,
- RDS 表的`Blob`或者`Text`字段
- 某对象存储服务，如 Amazon OSS，七牛云存储等
- Share Point 服务
- 其他提供公共访问接口的存储服务

所有这些，对于上层应用几乎都是完全透明，也就是上层应用仅仅知道自己向某个路径的对象
设置了某些元数据，或者二进制内容，但是它完全不关心这些信息具体存放到哪里，也不关心
访问效率等细节，这些都应该是 Walnut 服务器挂载层需要考虑的事情。

事实上，上述的设想就是 Walnut 服务器设计的最初最根本动机。

对于`WnObj`的操作，我们提供了标准命令 `o` 下面是它的常用方法，你会看到操作数据有多么简单

```js
//--------------------------------
// 读取对象元数据
//--------------------------------
var obj = $wn.execAsJson("o ~/path/to/file.txt @json -cqn");
// > obj = {id:"s2pqbka6j0huaq3bojm80c5dlu", nm:'file.txt', ...}
var obj = $wn.execAsJson("o id:s2pqbka6j0huaq3bojm80c5dlu @json -cqn");
// > obj = {id:"s2pqbka6j0huaq3bojm80c5dlu", nm:'file.txt', ...}
var obj = $wn.execAsJson("o @fetch ~/path/to/file.txt @json -cqn");
// > obj = {id:"s2pqbka6j0huaq3bojm80c5dlu", nm:'file.txt', ...}
var objs = $wn.execAsJson("o @get s2...lu ut..hk7bgnhfg @json -cqn");
// > objs = [{..WnObj..}, {..WnObj..}]

//--------------------------------
// 查询对象，通过 @query 子命令，它的语法基本与 《查询条件JSON语法》一致
// 这里就不赘述了，你就需要知道，根据挂载介质不同，
// 挂载层会将其转换为目标介质支持的查询语法
//--------------------------------
var qs = JSON.stringify({ nm: "^Z", age: "[20,]" });
var objs = $wn.execAsJson("o ~/tmp @query '" + qs + "' @json -cqn");
// > 返回 ~/tmp 目录下所有 nm 以 'Z' 开头，age 大于等于 20 的记录
// > objs = [{..WnObj..}, {..WnObj..}]
//--------------------------------
// 有限条目查询
var qs = JSON.stringify({ nm: "^Z", age: "[20,]" });
var objs = $wn.execAsJson(
  "o ~/tmp @query '" + qs + "' -limit 5 -skip 10 -sort '{ct:1}' @json -cqn"
);
// > 返回 ~/tmp 目录下 nm 以 'Z' 开头，age 大于等于 20 的记录，
// > 按 ct ASC 排序跳过前10条，最多输出 5条
// > objs = [{..WnObj..}, {..WnObj..}, {..WnObj..}, {..WnObj..}]
//--------------------------------
// 有限条目查询（带上分页信息）
var qs = JSON.stringify({ nm: "^Z", age: "[20,]" });
var result = $wn.execAsJson(
  "o ~/tmp @query '" +
    qs +
    "' -limit 5 -skip 10 -sort '{ct:1}' -pager @json -cqn"
);
// > 返回 ~/tmp 目录下 nm 以 'Z' 开头，age 大于等于 20 的记录，
// > 按 ct ASC 排序跳过前10条，最多输出 5条，并且输出的结果带上分页信息
// > result = {
//     list: [{..WnObj..}, {..WnObj..}, {..WnObj..}, {..WnObj..}],
//     pager: {
//        pageNumber: 3,   // 当前是第 3 页(1 Base)
//        pageSize: 5,     // 每页最多 5 条记录
//        pageCount: 3,    // 符合条件记录一共可以分作 3 页
//        totalCount: 14,  // 符合条件记录一共有 14 条
//        count: 4,        // 本页记录共有 4 条
//     }
// }
//--------------------------------
// 写入对象内容
//--------------------------------
sys.exec('echo "hello world" > ~/path/to/hello.txt');
sys.exec("str > ~/path/to/hello.txt", "hello world");
//--------------------------------
// 读取对象内容
//--------------------------------
var re = sys.exec2("cat ~/path/to/hello.txt");
// > re = "hello world\n"
//--------------------------------
// 删除对象
//--------------------------------
sys.exec("rm ~/path/to/hello.txt");
sys.exec("o ~/path/to/hello.txt @delete @json -cqn");
// 删除多个对象(先查出来再删除)
var deletedObjs = sys.exec("o ~/tmp @query 'nm:\"^X\"' @delete @json -cqn");
// > 删除 ~/tmp 目录下所有 nm 以 'X' 开头的记录，并将它们作为输出返回
// > deletedObjs = [{..WnObj..}, {..WnObj..}]
```

> `o` 命令的功能非常强大，可以应对多数数据操作场景
> 我们不可能在一篇文档穷尽它所有的用法
> 执行 `man o` 可以看到它的完整操作手册

### 获取唯一 ID

```js
// 快速雪花算法（10位随机）
sys.exec("val @gen snowQ::10");
// > mb1v610hb0sa9r4chp
// 其中 [mb1v610h] 36进制毫秒时间戳 [b0sa9r4chp]10位36进制随机数

// 快速雪花算法（1位随机）
sys.exec("val @gen snowQ::1");
// > mb1v6xff8
// 其中 [mb1v6xff] 36进制毫秒时间戳 [8]1位36进制随机数

// 一次生成3个ID
sys.exec("val @gen -n 3 snowQ::1");
// > ["mb22ge7e9", "mb22ge7eq", "mb22ge7eu"]

// UUID
sys.exec("val @gen UU32");
// > 6v0irqjps2hv3rg9vik4247nff

// 序列号
sys.exec("val @gen seq:M:4:~/path/to/yyyy-MM-dd#my_seq");
// > M0423
// > 根据 ~/path/to/2025-05-26 这个对象(参见标准对象描述)
// > 的 my_seq 字段值获取下一个序列号，并限定最小宽度为4。
// > 最后拼合前缀 `M` 得到的一个序列号，
// > 通常用作数据记录主键或者唯一性索引键
// > 其中 2025-05-26 是今日日期, 格式符合 yyyy-MM-dd
// > 因此如果按月滚动需要，你的对象路径通常是 ~/path/to/yyyy-MM
// > 如果你想让序列全局滚动, 那么你的对象路径通常是 ~/path/to/some_name#my_seq
```

### 处理日期

Walnut 提供了专门处理日期命令 `datex`，它的主要用法:

```js
// 获取当前时间
var re = sys.exec2("datex");
// re='2025-05-24 17:04:29'

// 当前时间向后3个月
var re = sys.exec2("datex @month -i 3");
// re='2025-08-24 17:11:39'

// 当前时间向前3个月
var re = sys.exec2("datex @month -i -3");
// re='2025-02-24 17:11:39'

// 3月份的当前日期（设置月份为3月）
var re = sys.exec2("datex @month 3");
// re='2025-03-24 17:11:39'

// 指定日期为本月10号,时间设置为 0 点
var re = sys.exec2("datex @day 10 @time 0");
// re='2025-05-10 00:00:00'

// 指定日期为本月10号,时间设置为 23:59:59
var re = sys.exec2("datex @day 10 @time 23:59:59");
// re='2025-05-10 23:59:59'

// 当前时间向前偏移10天
var re = sys.exec2("datex @day -i -10");
// re='2025-05-14 17:41:06'

// 将当前日期设置为本月最后一天
var re = sys.exec2("datex @day -i 32 -inmonth");
// re='2025-05-31 17:40:48'
// > `-i 32` 会确保一定会将日期移动到本月下一个月
// > `-inmonth` 会确保一定会将日期向前移动到本月最后一天

// 用另外一种方法得到本月最后一天
var re = sys.exec2("datex @day 1 @month -i 1 @day -i -1");
// re='2025-05-31 18:00:49'
// > `@day 1 @month -i 1` 将日期设置为下月1号
// > `@day -i -1` 将日期前移一天自然就是本月最后一天了

// 指定输出格式化方式
var re = sys.exec2("datex @fmt d MMM yy");
// re='24 May 25'

// 指定日期
var re = sys.exec2("datex @set 2023-09-21");
// re='2023-09-21 00:00:00'

// 当前时间之后的1天
var re = sys.exec2("datex @set now+1d");
// re='2025-05-25 17:29:46'

// 绝对毫秒数时间戳
var re = sys.exec2("datex @set 1694368557435");
// re='2023-09-11 01:55:57'
```

> `datex` 命令的功能还有更多格式化，时区，节假日相关的功能
> 执行 `man datex` 可以看到它的完整操作手册

## 最后一些注意事项

1. 如果能用高级接口，就用高级接口，譬如能用 `$wn.execAsJson` 就不要用 `sys.exec2  + JSON.parse`
