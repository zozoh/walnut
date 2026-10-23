# 过滤器简介

`@search` 按条件搜索一组视频。

> 搜索条件等更详细参数请参见 [阿里云搜索媒资信息文档](https://help.aliyun.com/document_detail/86071.html)

# 用法

```bash
aliyunvod @search [搜索类型] [-fields 字段,...] [-match 条件] [-sort 排序] [-pn N] [-pgsz N] [-scroll 标识] [-as 格式] [-cqn]
       # 搜索类型    # 默认 video
  -fields    # 指定要输出的额外字段，如 Title,CoverURL
  -match     # 设置过滤条件，详见阿里云文档
  -sort      # 排序条件，详见阿里云文档
  -pn        # 翻页：第几页，1 为第一页
  -pgsz      # 翻页：页大小，默认 20
  -scroll    # 翻页：翻页标识，第一次搜索结果会带
  -as        # 输出格式：page 转换成 walnut 标准翻页格式 | raw 原始格式
  -cqn       # JSON 格式化开关
```

# 示例

```bash
# 获取原始的查询结果
aliyunvod @search -fields Title,CoverURL,Duration -pn 2 -pgsz 2 -cqn
# {
#    requestId: "1F1BCA78-F48D-49C9-AE53-2E8BE46AE4AD",
#    scrollToken: "f0d22725e93eebdcc267fd3f42739f34",
#    total: 5,
#    mediaList: [{
#       mediaType: "video",
#       mediaId: "0063f3e137774e748e2d8ga506cq1ef5",
#       ...
#    }]
# }
```
