# 命令简介 

 `seq create` 根据条件生成一个序列

## 用法

    seq create -tmpl $tmpl 
               [-start 1] 
               [-match 'pid:"XXXX"']
               [-sort 'ct:1']
               [-force]
               [-key u_code]
               [-vars]
## 参数说明

tmpl    渲染模板,支持seq(当前序号), obj(当前列表元素), 其他变量(通过vars传入)
start   序列的起始值,默认1
match   列表模式, 查询一组符合条件的对象
sort    列表模式下,对象的排序方式,默认ct:1
key     列表模式下,对象用于存储序列值的键,默认u_code
force   列表模式下,是否覆盖原有的键值,默认false
vars    额外变量表,需要是json, 或者从标准输入获取

## 示例

简单序列

```
>:  seq create -tmpl '${seq}'
1,2,3,4,5,6,7,...
```

生成选手赛号

```
// 赛项对象id:xxx, 其中u_code_prefix=A
>: obj id:xxx | seq create -tmpl '\${u_code_prefix}\${seq<int:%03d>}\${obj.u_sex}' -vars 'prefix:"T"' -match 'pid:"48mv1tu9a2j5arrcgiiub6slhu",u_pj:"10KM"' -vars
{
   updated: 0,
   nochange: 5
}
// 生成的序号类似于 A001M, A002F ...
// 并设置到对象的u_code属性
```