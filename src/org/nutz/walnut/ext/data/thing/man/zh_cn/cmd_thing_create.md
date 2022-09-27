# 命令简介 

`thing create` 创建记录

# 用法

```bash
thing [TsID] create 
        [-fields "{..}"]     # 其他字段，上述参数如果有值，会将对应字段覆盖
                             # 如果是数组，则创建多个对象
        [-unique "phone"]    # 指定一个唯一字段，如果设置，添加的时候如果发现已经存在
                             # 则改成更新
        [-process TMPL]      # 进度模式，只有当 -fields 为数组时，才有效
                             # 表示不要输出创建的数据详情，而是输出创建的进度
                             # 参数值是一个模板，占位符用 ${xxx} 来表示
                             # 占位符会用创建的对象作为上下文，唯一特殊的是
                             # ${P} 这个是上传比例字符串，格式为 %[10/982]
                             # 由导入命令来填充
                             # ${I} 则表示创建的记录下标（1base）
        [-after TMPL]        # 当创建一条数据后，执行这个命令模板，用数据作为上下文
                             # 如果执行异常，则会停止后续执行
        [-fixed {..}]        # 固定字段，所有创建的项目都会增加给定的字段
#----------------------------------------------------
- 当前对象可以是一个 thing 或者 ThingSet
- 如果是一个 thing，相当于是它的 ThingSet
```

# 示例 

```bash
# 创建一个名为 ABC 的 thing
thing xxx create ABC
    
# 创建一个名为 ABC 且有一个简要说明的 thing
thing xxx create 'ABC' -brief 'This is abc'
# or
thing xxx create ABC -fields "{th_brief:'This is abc'}"
# or
thing xxx create -fields "{th_nm:'ABC', th_brief:'This is abc'}"
    
# 创建一个匿名的 Thing 并指明分类
thing xxx create -cate xxx
```