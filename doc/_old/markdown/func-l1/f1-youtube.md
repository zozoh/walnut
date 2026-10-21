---
title: 第三方接口·Youtube说明
author: zozoh
---

# 数据布局

```bash
/home/demo/
#--------------------------------------------------
|-- .xapi/youtube/demo/
|   |-- config.json       # XAPI 的配置文件
#--------------------------------------------------
|-- .domain/youtube/demo/
|   |-- youtube.json          # 关键配置信息
|   |-- playlists.json        # 梳理过的播放列表关键信息
#--------------------------------------------------
|   |-- results/              # 缓存 XAPI 的返回结果
|   |   |-- channels.json     # 频道列表
|   |   |-- playlists.json    # 频道内播放列表
```

# 获取当前频道的信息

> `Youtube`将当前频道全部视频存放到里一个内置的`上传列表中`，
> 所谓 `上传列表` 就是一个 `playlist`，可以通过下面的方式获取

```bash
#
# 1. 获取自己的频道信息（需要频道列表）
# 频道列表 ID 需要从 youtube.json 里读取，如果不存在，则向用户询问
#
demo:> xapi req youtube demo channels -vars 'id:"${channelId}",part:"${channelPart}"' -url
{
    "kind": "youtube#channelListResponse",
    "etag": "03crC..1iuo",
    "pageInfo": {
        "totalResults": 1,
        "resultsPerPage": 5
    },
    "items": [{
      "snippet" : {
        "title": "FREE IS THE MEANING OF LIFE",
      },
      "contentDetails" : {
        "relatedPlaylists": {
            "likes": "",
            "favorites": "",
            "uploads": "UUib3..Ed35A"
        }
      }
    }]
}
#
# 2. 将 uploads 播放列表 ID 保存到 youtube.json
#
demo:> cat ~/.domain/youtube/demo/youtube.json
{
  channelId : "UCib..d35A",
  uploadsPlaylistId : "UUib3..d35A",
}
```

# 获取频道内全部播放列表

> 因为频道内播放列表不经常变化，因此可以做一下缓存

```bash
#
# 1. 获取自己的频道信息（需要频道列表）
# 频道列表 ID 需要从 youtube.json 里读取，如果不存在，则向用户询问
#
demo:> xapi youtube demo playlists -vars 'channelId:"${channelId}",part:"${playlistPart}"'
{
    "kind": "youtube#channelListResponse",
    "etag": "03crC..1iuo",
    "nextPageToken": "CAoQAA",
    "prevPageToken": "CAUQAQ",
    "pageInfo": {
        "totalResults": 1,
        "resultsPerPage": 5
    },
    "items": [{
      "kind": "youtube#playlist",
      "etag": "eQZBlc-1YpYE3NwfDd5C_oaku3U",
      "id": "PLqn..9WD",
      "snippet" : {..},
      "status": {
        "privacyStatus": "public"
      },
      "contentDetails" : {
         "itemCount": 2
      },
      "player" : {..}
      }
    }]
}
#
# 2. 将播放列表 ID 保存到 playlists.json
#
demo:> cat ~/.domain/youtube/demo/playlist.json
[{
  id : "PLqn...5xrY",
  title : "xxxx",
  description : "xxx",
  thumb_url : "https://i.y..ult.jpg",  # <-- "high"
  itemCount : 2
}]
```

# 获取视频列表

> 首先获取播放列表项目，收集视频ID，并一次性获取全部视频信息

```bash
#
# 1. 获取播放列表的项目
#
demo:> xapi youtube demo playlistItems -vars 'playlistId:ID,part:"contentDetails,status"'
{
  "kind": "youtube#playlistItemListResponse",
  "etag": "vv2JXNjo8vPPi6NbRyhNvCceR_A",
   "items": [{
      "kind": "youtube#playlistItem",
      "etag": "mnwm7YOb-lqEBoczN-7uNGwyj-o",
      "id": "VVVpYjNVZkU4N1E1SExLdnBwcUVkMzVBLkdxVjVOSzFkUWpr",
      "snippet" : {
        "publishedAt": "2021-03-11T07:56:08Z",
        "channelId": "UCib..Ed35A",
        "title": "1m海洋奇缘",
        "description": "1m海洋奇缘是一个测试视频",
        "thumbnails": {..},
        "channelTitle": "FREE IS THE MEANING OF LIFE",
        "playlistId": "UUib3UfE87Q5HLKvppqEd35A",
        "position": 0,
        "resourceId": {
            "kind": "youtube#video",
            "videoId": "GqV5NK1dQjk"
        },
        "videoOwnerChannelTitle": "FREE IS THE MEANING OF LIFE",
        "videoOwnerChannelId": "UCib3UfE87Q5HLKvppqEd35A"
      },
      "contentDetails": {
          "videoId": "GqV5NK1dQjk",
          "videoPublishedAt": "2021-03-11T08:14:44Z"
      },
      "status": {
          "privacyStatus": "public"
      }
    }]
}
#
# 得到一个 videoId 的列表，然后获取详情
#
demo:> xapi youtube demo videos -vars 'id:"ID1,ID2",part:"${videoPart}"'
{
  "kind": "youtube#playlistItemListResponse",
  "etag": "vv2JXNjo8vPPi6NbRyhNvCceR_A",
  "items": [{
      "kind": "youtube#playlist",
      "etag": "eQZBlc-1YpYE3NwfDd5C_oaku3U",
      "id": "uxlJ1h1Vouo",
      "snippet" : {
        "publishedAt": "2021-03-11T07:56:08Z",
        "channelId": "UCib..Ed35A",
        "title": "1m海洋奇缘",
        "description": "1m海洋奇缘是一个测试视频",
        "thumbnails": {
            "default": {
                "url": "https://i.ytimg.com/vi/uxlJ1h1Vouo/default.jpg",
                "width": 120,
                "height": 90
            },
            "medium": {
                "url": "https://i.ytimg.com/vi/uxlJ1h1Vouo/mqdefault.jpg",
                "width": 320,
                "height": 180
            },
            "high": {
                "url": "https://i.ytimg.com/vi/uxlJ1h1Vouo/hqdefault.jpg",
                "width": 480,
                "height": 360
            },
            "standard": {
                "url": "https://i.ytimg.com/vi/uxlJ1h1Vouo/sddefault.jpg",
                "width": 640,
                "height": 480
            },
            "maxres": {
                "url": "https://i.ytimg.com/vi/uxlJ1h1Vouo/maxresdefault.jpg",
                "width": 1280,
                "height": 720
            }
        },
        "channelTitle": "FREE IS THE MEANING OF LIFE",
        "categoryId": "22",
        "liveBroadcastContent": "none",
        "localized": {
            "title": "1m海洋奇缘",
            "description": "1m海洋奇缘是一个测试视频"
        }
      },
      "status": {..},
      "contentDetails" : {
        "duration": "PT1M2S",
        "dimension": "2d",
        "definition": "hd",
        "caption": "false",
        "licensedContent": false,
        "contentRating": {},
        "projection": "rectangular"
      },
      "player" : {..}
    }],
  "pageInfo": {
      "totalResults": 2,
      "resultsPerPage": 2
  }
}
```

# 文件格式

## config.json

> 记录了 `Youtube` 的访问 API KEY

```js
{
  "apiKey" : "Mq6a...Faj8"
}
```

## youtube.json

> 记录了 `Youtube` 当前频道的关键配置信息

```js
{
  // 配置名称，相当于 ~/.domain/youtube/${domain}/ 的目录名
  domain : "demo",
  // 播放列表或者视频缩略图的尺寸规格
  // 默认为 high
  // 可选值为： default|medium|high|standard|maxres
  thumbType : "high",
  // 查询时，最多返回多少结果，最大为 50
  maxResults : 50,
  //
  // 频道 ID 由用户填入
  //
  channelId : "UCib..d35A",
  // 根据 channelId 自动获取
  channelTitle: "FREE IS THE MEANING OF LIFE",
  // 获取频道列表时，需要显示的字段
  channelPart : "snippet,contentDetails,statistics",
  //
  // 上传列表的 ID （自动从 API 获取）
  //
  uploadsPlaylistId : "UUib3..d35A",
  // 获取播放列表时的 part
  playlistPart : "snippet,contentDetails,status,id,player,localizations",
  // 获取视频的 part
  videoPart : "snippet,contentDetails,status,id,player"
}
```

## playlists.json

> 记录了 `Youtube` 当前频道的所有播放列表的

```js
[{
  id : "PLqn...5xrY", 
  title : "xxxx",
  description : "xxx",
  thumbUrl : "https://i.y..ult.jpg",  // <- thumbType
  itemCount : 2
}]
```

# 客户端代码

上述的逻辑是可以放到服务器端运行的，但是由于线路的问题，发起请求通常在本地进行。
因为本地可以使用代理访问互联网。

为了让上述逻辑更加方便，`Walnut` 提供了 JS 库

 Method                 | Description
------------------------|--------------------
`Wn.Youtube.loadConfig` | 加载 youtube 配置信息
`Wn.Youtube.playlists`  | 获取可用播放列表
`Wn.Youtube.videos`     | 获取视频列表
`Wn.Youtube.EditConfig` | 打开 youtube 配置编辑对话框

## 获取配置信息

```js
// 根据 channelId 加载必要的配置信息并返回给用户
let config = await Wn.Youtube.loadConfig({
  channelId,      // 指定一个新的 channelId
  force = false   // 是否强制刷新缓存
})
//
// 如果配置文件 youtube.json 未声明 channelId
// 会弹出一个对话框来要求用户输入
//
// 如果传入一个不同的 channelId，则会导致配置信息被自动刷新
//
// 如果不存在 channelTitle 和 uploadsPlaylistId 字段，也会重现读取
// 因为其他字段可以设置默认值
//
```

## 获取播放列表

> 不指定的 playlist ID 将会采用 uploadsPlaylistId

```js
// 获取所有的播放列表，这个函数实际上是不断调用 playlists 来实现的
// 最后将结果存放到 playlists.json 里
let list = await Wn.Youtube.getAllPlaylists(config, {
  force = false   // 是否强制刷新缓存
})

// 分页读取播放列表
let {list, next, prev} = await Wn.Youtube.getPlaylists(config, {
  pageToken
})
// prev 为上一页的 pageToken，第一页的话，则为空
// next 为下一页的 pageToken，最后一页的话，则为空
// list 为一个这样格式的列表，这个列表会存放在 youtube.json 里
[{
  id : "PLqn...5xrY",
  title : "xxxx",
  description : "xxx",
  thumbUrl : "https://i.y..ult.jpg",  // <- thumbType
  itemCount : 2
}]
```

## 获取视频列表

```js
// 不指定的 playlist ID 将会采用 uploadsPlaylistId
let {list, next, prev} = await Wn.Youtube.getVideos(config, playlistId, {
  pageToken
})
// prev 为上一页的 pageToken，第一页的话，则为空
// next 为下一页的 pageToken，最后一页的话，则为空
// list 为一个这样格式的列表
[{
  id : "PLqn...5xrY",
  title : "xxxx",
  description : "xxx",
  publishedAt : "2021-03-11T08:03:31Z",
  tags : ["xx", "xx", "xx"],
  thumbUrl : "https://i.y..ult.jpg",  // <- thumbType
  defaultLanguage : "zh-CN",
  defaultAudioLanguage : "zh-CN",
  duration : "PT17M51S",
  du_in_sec : 943,
  du_in_str : "17:51",
  definition : "hd",
  categoryId : "25"
}]
```

# 参考文档

- [YouTube Data API Overview](https://developers.google.com/youtube/v3/getting-started)
- [YouTube Data API Reference](https://developers.google.com/youtube/v3/docs)