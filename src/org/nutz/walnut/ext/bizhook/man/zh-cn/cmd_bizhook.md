命令简介
======= 

`bizhook` 读取一个配置文件，根据配置文件的内容，对输入的对象执行某些特殊的命令。

配置文件内容为：

```json
{
  // 预留一层，以便增加更多全局配置
  hooks : [{
    // 匹配项目如果为空，则表示一定是匹配的
    // 每个条件都为 or 的关系，元素是一个 map，各个字段是 and 的关系
    match: [{
      // 半角逗号`,`分隔的键值，会 pick 出一个对象
      // 如果声明 ".." 则表示全部传入对象
      // 字符串值表示一个正则表达式，来匹配值
      "key1,key2" : '^((g2)|(g3))$'
      // 半角竖线`|`分隔表示获取值 fallbackNil 的候选键
      // 可以用对象来表示更复杂的匹配 @see org.nutz.validate.NutValidate
      "key3|key4" : {}
    }],
    // 执行命令模板默认有上下文变量由管道或者 `-vars` 参数传入
    commands : ["echo '${obj.nm}' > abc.txt"]
  }]
}
```

用法
=======

```bash
# 子命令
bizhook {ConfigPath} test      # 测试 bizhook
bizhook {ConfigPath} run       # 运行 bizhook
```

`bizhook` 命令有两种模式

- `匹配模式` : (默认)会寻找到第一个可匹配的钩子项执行
- `批量模式` : (`-batch`)会寻找所有匹配的钩子依次执行

同时 `-tail` 表示从钩子列表后面开始查找。
声明 `-limit 2` 在批量形态中可以限定总共寻找到钩子的个数

示例
=======

```bash
# 根据指定对象的元数据，执行一个 hook
bizhook ~/myhook.json run id:4t8a..23a0

# 根据多个对象元数据分别执行 hook
bizhook ~/myhook.json run a.txt b.txt c.txt

# 根据对象内容执行 hook
cat demo.json | bizhook ~/myhook.json run
```
