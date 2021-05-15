# 命令简介 

`thing query` 查询数据

# 用法

```bash
thing [TsID] query 
            [Condition Map]
            [-content]
            [-t "c0,c1,c2.."]
            [-pager]
            [-limit 10]
            [-skip 0]
            [-sort "nm:1"]
            [-sha1 thumb,...]
            [-obj]
            [-cqn]
#
# 参数说明:
#
- [TsId] 当前对象可以是一个 `thing` 或者 `ThingSet`
            如果是一个 `thing`，相当于是它的 `ThingSet`
- [Consition Map] 查询条件如果不包括 `th_live`，
                    那么默认将设置为 `th_live=1` 表示所有可用的 `thing`
- content 表示同时同时输出数据的内容，记做 `content` 字段
- t 表示按照表格方式输出，是 query 的专有形式，内容就是半角逗号分隔的列名
- pager  显示分页信息，JSON 输出时，输出类似 {list:[..],pager:{..}} 的格式
            在 limit 小于等于 0 时，本参数依然无效
- limit  限制输出的数量，默认 100
- skip   跳过的对象数量，默认 0
- sort 排序字段
- sha1 用半角逗号分隔一组字段，这些字段是关联对象，要输出 $key_sha1 的内容指纹
- obj 查询列表如果是多个，则只输出第一个对象，如果列表为空，则返回 null
```