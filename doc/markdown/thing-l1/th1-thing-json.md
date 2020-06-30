---
title: Thing·配置文件详解
author: zozohtnt@gmail.com
---

--------------------------------------
# uniqueKeys

声明本数据集可以有哪些唯一键。当 `create/update` 时，会根据这个设置检查以避免重复。

```js
[{
  // 一个元素的数组表示单个键
  name : ["key1"],
  // 数据不能为空，默认false,只有提交数据有值才检查
  required : true
}, {
  // 复合唯一键
  name : ["key1", "key2"],
  required : false
}]
```

如果一个键的值有三个种可能：

Pri | Name        | Defination
----|-------------|-------------------
 0  | `undefined` | `!containsKey(key)`
 1  | `Exists`    | `get(key)==null`
 2  | `NoNull`    | `get(key)!=null`

> Pri(0) is the highest priority.

这里判断重复的逻辑是：

```bash
如果所有的键有任何一个不是 `NoNull` 进入下面的检查逻辑：
  # 如果是复合唯一键 {
    # 如果存在 `Exists` 且有 `NoNull` 的键
      throw e.thing.ukey.partly

    # 如果 required == true
      throw e.thing.ukey.required  
  }
  # 否则就是单个唯一键 {
    forUpdate:
      # 如果 undefined:
        则无视
    # 如果 required == true
      throw e.thing.ukey.required
  }
}
```

--------------------------------------
# fields

声明本数据集的字段约束。当 `create/update` 时，会根据这个设置检查字段值的合法性。

```js
{
  key : "xxx",       // 字段名
  validate : {..},   // 检查方式
}
```

这里的`validate`格式规范如下：

```js
{
  // 针对字符串型的值，检查前是否要预先去掉左右空白
  trim : true,
  // 数字区间
  intRange : "(10,20]",
  // 日期范围的区间
  dateRange : "(2018-12-02,2018-12-31]",
  // 验证值的字符串形式，支持 "!" 开头
  regex : "^...$",
  // 确保值非 null
  notNull : true,
  // 针对字符串的值，最大长度不超过多少
  maxLength : 23,
  // 针对字符串的值，最小长度不能低于多少
  minLength : 5,
}
```

- 所有项目都是 `AND`的关系
- 检查的顺序会是 `trim > notNull > max/minLength > 其他`

--------------------------------------
# linkKeys

链接键定义了一映射表，将本字段的值映射到其他字段，甚至其它数据集的某几个字段。
如果发生了 `create/update`， 会自动修改生/成对应的字段。
如果发生了 `delete` 那么根据配置，会自动删除对应其他表的记录

```js
linkKeys : {
  //----------------------------------
  // 详细说明
  "key1" : {
      // 一组限制条件，如果匹配上了，本条规则才会继续执行 match 等操作
      // 每个条件是一个 Map， 里面的条件是 AND
      // 数组之间是 OR
      // 这个匹配的是更新前的数据记录
      testPrimary : [{
          "name" : "!isEmpty",
          "age" : {
            name: "inRange",
            args: "[10, 13)"
          }
        }],
      // 格式同上，匹配的是新修改的元数据表的内容
      testUpdate : [..],
      // 【选】如果不指定，那么就不检查
      // 如果检查不通过，则根据下面的配置项决定是抛错，还是忍耐
      match  : '^((g2)|(g3))$',
      // 严格模式开关，如果开启严格模式，那么对于 match，必须匹配才可以
      // 当然如果本身值就是 null，那么都是无视的
      // 如果是非严格模式，则继续后续的执行，那么匹配项的 g2..3 的值就没有了
      strict : true,
      // 【选】如果不指定，则使用当前数据对象
      // 这里可以指定一个过滤条件，符合这样条件的数据会被挑选出来
      // 执行后面的 set
      target : {
          // 【选】另外一个 ThingSet 的路径
          // 如果不指定，就是自己
          thingSet : '~/xxx',
          // 【选】过滤条件，如果不指定则为全部数据
          // 每个过滤项的值，遵守 set 字段值的规范
          // 上下文为当前的数据对象
          filter   : {..}
      },
      // 设置值的方法
      set : {
          // 值 "@val" 表示原始值，将直接保留原来值的类型
          "ta_key1" : "@val",
          // 如果声明了 match，那么会在上下文添加 g1-n 的变量
          // 类型都是字符串
          "ta_key2" : "@g2",
          // 你也可以指明类型:
          //  - int     : Integer : 整型
          //  - float   : Float   : 浮点
          //  - boolean : Boolean : 布尔
          //  - string  : String  : 字符串
          // 默认为 string
          // 这种语法 @val 形式也支持
          "ta_key3" : "@g3:int"
          // 如果想组合复杂一点的值，可以
          // 这种形式下，值只能被设置成字符串
          // 即，非 @xx 形式的值，会被认为是字符串模板
          "ta_key4" : "${g2}_${g4}"
      }
  },
  //----------------------------------
  // 将自己分解成多个值，分别设置到其他字段
  "key2" : {
      match : '^([A-Z]+)-([A-Z]{2})([0-9]+)-([0-9]+)$',
      set : {
          "dev_tp" : "@g1",
          "spl_nm" : "@g2",
          "spl_md" : "@g3",
          "spl_nb" : "@g4:int"
      }
  },
  //----------------------------------
  // 自己的值设置到另外的数据集的多个记录
  "key3" : {
      target : {
          thingSet : "~/xxx",
          filter : {
              'ta_id' : "@id"
          }
      },
      set : {
          "ta_key3" : "@val"
      }
  },
  //----------------------------------
  // 当自己的值发生改变时，运行一段脚本
  // ！注意，run 和 target/set 是互斥的，
  // ！run 声明了， target/set 就被无视了
  "key4" : {
    // 【选】匹配这个表达式才会运行命令模板
    // 不指定的化，只要发生改动就会运行
    match  : '^((g2)|(g3))$',
    // 命令模板的上下文是整个 Thing 对象
    // 同时加上 ${@val} ${@g2} ${@g3}... 等占位符
    run : ['jsc xxxx']
  }
}
```


--------------------------------------
# onCreated

当创建一个数据后，还可以执行一个后续脚本。
脚本执行的模板，接受当前 thing 对象为上下文。

```js
{
  onCreated : ["jsc /jsbin/xxx.js -vars 'id:\"${id}\"'"]
}
```

--------------------------------------
# onUpdated

当更新了一个数据后，还可以执行一个后续脚本。
脚本执行的模板，接受当前 thing 对象为上下文。

```js
{
  onUpdated : ["jsc /jsbin/xxx.js -vars 'id:\"${id}\"'"]
}
```
--------------------------------------
# onBeforeDelete

在删除一个数据前，可以执行一个预处理脚本。
脚本执行的模板，接受当前 thing 对象为上下文。

```js
{
  onBeforeDelete: ["jsc /jsbin/xxx.js -vars 'id:\"${id}\"'"]
}
```

这个脚本的如果在错误输出写入数据，那么则会导致删除被终止，并抛出异常。
错误输出里的内容会被当作异常的解释性原因展现给客户。

--------------------------------------
# onDeleted

在删除一个数据后，可以执行一个脚本。
脚本执行的模板，接受当前 thing 对象为上下文。

```js
{
  onDeleted: ["jsc /jsbin/xxx.js -vars 'id:\"${id}\"'"]
}
```

