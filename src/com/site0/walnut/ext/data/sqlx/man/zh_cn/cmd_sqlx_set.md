# 过滤器简介

`@set` 设置上下文的某个变量

变量值支持下面的格式：

```bash
#---------------------------------
# 自增序列: 带有日期信息，序号每日归零
# Seq(9)
# - prefix = M
# - n = 9
# - seq.parentPath = ~/path/to
# - seq.name = yyyy-MM-dd.'txt'
# - seq.key = my_seq
seq:M:9:~/path/to/yyyy-MM-dd.'txt'#my_seq
> M 000 003 423

#---------------------------------
# 日期序列: 带有日期信息，序号每日归零
# SeqD('FX',9)
seqD:FX:9:~/path/to/yyyy-MM-dd.'txt'#my_seq
> FX 240501 000 005 902

#---------------------------------
# 小时序列: 带有日期以及小时信息，序号每日归零
# SeqHH('BMH',5)
seqHH:BMH:5:~/path/to/yyyy-MM-dd.'txt'#my_seq
> BMH 24050109 05 902

#---------------------------------
# 简易雪花: 前缀+36位日期串+随机数
# snowQ('XT',4)
snowQ:XT:4
> XT lvwjlrvf A8mz

#---------------------------------
# 简易雪花: 前缀+可读日期串+随机数
# snowQD('K',4)
snowQD:K:4
> K 20240921231025765 tN7f9r

#---------------------------------
# UTC 时间字符串,格式为
#    utc:{? 偏移}:#{? 格式}
# 如果不指定格式，则默认为 'yyyy-MM-dd HH:mm:ss.SSS'
# 会将当前时间转换为 
# 譬如:
date::yyyy-MM-dd HH:mm:ss.SSS
> 2025-12-23 12:22:45.872

date:+1d#yyyy-MM-dd HH:mm:ss.SSS
> 2025-12-24 12:22:45.872

#---------------------------------
# 时间戳
#    ams:{? 偏移}
# 如果不指定格式，则默认为 'yyyy-MM-dd HH:mm:ss.SSS'
# 会将当前时间转换为 
# 譬如:
ams
> 1754917896417

ams:+1d
> 1754917896417

#---------------------------------
# IPV4
ipv4
> 192.168.1.23

#---------------------------------
# UUID 的 32位数字表示
# uu32
uu32
> 7l075gcbreii6orfl8lh227k3d

#---------------------------------
# 从前序结果集获取数据
=the_id
> 7l075gcbreii6orfl8lh227k3d

#---------------------------------
# 静态值
:hello world
> hello world
```

# 用法

```bash
sqlx @set 
  [varName]           # 变量名称
  [varValue]          # 变量值
  [-to list|map|all]  # 为列表还是map设置，如果不指定，则表示 all
  [-savepipe xx]      # 将生成的值存入【过滤管线上下文】, 存入的键是动态值
                      # 譬如 "pet.${id}" 表示用当前对象（无论是list还是map）
                      # 作为上下文，渲染一个键，这样就能动态生成多个键了
  [-alias xxx]        # 【选】变量别名，通过这个可以将值设置到多个键中，
                      # 每个键用半角逗号分隔，譬如 note,
  [-when ...]         # 指定一个 AutoMatch 的条件，匹配这个条件
                      # 才会设置 alias
  [-explain]          # 自动展开要更新元数据的【宏】
```

# 示例

```bash
# 自动为所有要插入的对象设置 ID
cat ~/list.json | sqlx @vars '=list' -as list @set id 'snowQ::10' -to list @exec pet.insert -p;

# 自动为所有对象设置 ID 并且，如果对象 type='dog' 则为其 note 字段也设置这个 ID
cat ~/list.json | sqlx @vars '=list' -as list @set id 'snowQ::10' -alias note -when '{type:"dog"}' -to list @exec pet.insert -p;

```


