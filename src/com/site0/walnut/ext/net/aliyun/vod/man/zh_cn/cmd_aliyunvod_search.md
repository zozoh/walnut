命令简介
======= 

`aliyunvod search` 用来按条件搜索一组视频
    

用法
=======

```bash
aliyunoss search 
  [SearchType]               # 搜索类型，默认 video
  [-fields Title,CoverURL]   # 指定要输出的一些额外字段
  [-match xxx]               # 设置过滤条件，详见阿里云文档
  [-sort xxx]                # 排序条件，详见阿里云文档
  [-pn 1]                    # 翻页：第几页，1为第一页
  [-pgsz 20]                 # 翻页：页大小
  [-scroll xxx]              # 翻页：翻页标识，第一次搜索结果会带
  [-as page|raw]             # 输出格式，如果是page 会转换成 walnut 标准翻页格式
  [-cqn]                     # JSON 格式化
```

> 关于搜索条件等更详细参数说明请参看[阿里云搜索媒咨信息文档][aliyun-media-search]

示例
=======

```bash
# 获取原始的查询结果
demo:~$ aliyunvod search -fields Title,CoverURL,Duration -pn 2 -pgsz 2
{
   requestId: "1F1BCA78-F48D-49C9-AE53-2E8BE46AE4AD",
   scrollToken: "f0d22725e93eebdcc267fd3f42739f34",
   total: 5,
   mediaList: [{
      mediaType: "video",
      creationTime: "2020-05-02T06:24:37Z",
      mediaId: "0063f3e137774e748e2d8ga506cq1ef5",
      video: {
         videoId: "0063f3e137774e748e2d8ga506cq1ef5",
         ...

# 获取 Walnut 标准翻页查询结果
demo:~$ aliyunvod search -pn 2 -pgsz 2 -as page
{
   list: [{
      videoId: "0063f3e137774e748e2d8ga506cq1ef5",
      mediaId: "0063f3e137774e748e2d8ga506cq1ef5",
      mediaType: "video",
      creationTime: "2020-05-02T06:24:37Z"
   }, {
      videoId: "531f28ad2d31aa8d8794570a7c550e8c",
      mediaId: "531f28ad2d31aa8d8794570a7c550e8c",
      mediaType: "video",
      creationTime: "2020-05-02T06:23:28Z"
   }],
   pager: {
      pn: 2,
      pgsz: 2,
      pgnb: 3,
      sum: 5,
      skip: 2,
      nb: 2,
      pgc: 3,
      count: 2
   },
   scroll: "1f08c9f614157bdeb811f39dc2e356b8"
}
```

[aliyun-media-search]: https://help.aliyun.com/document_detail/86044.html?spm=a2c4g.11186623.2.25.d7871a3cAnKz4y