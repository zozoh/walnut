命令简介
======= 

`videoc` 视频转换命令
    

用法
=======

```
videoc [-vk] [-bv 3000] [-ba 64] [-vcodec libx264]
       [-preset ultrafast] [-fps 24] [-F] [视频文件路径]
       
-v       显示详细的转换日志
-k       不删除临时文件
-F       当文件名已`_`开头时默认不转换,加上此参数表示强制转换
-o       输出处理后的视频文件元数据

-mode   转换模式, 值是一个正则表达式 "preview_video|preview_image|main"
        preview_video : 输出预览视频
        preview_image : 输出预览图
        main          : 输出主视频
        
        如果只想输出预览视频和预览图，你可以 -mode "^preview"
        如果只想输出主视频，你可以 -mode "main"

-thumb  默认 64x64，如果输出预览图，将会将这个预览图转换成本视频的缩略图，这里给定预览图的尺寸 

// TODO wendal 来填一下咯
-bv     视频码率,默认3000,只能是整数
-ba     音频码率,默认64,只能是整数
-vcodec 视频编码,默认libx264
-preset 视频编码参数预设,默认ultrafast(可选 ultrafast,superfast, veryfast, faster, fast, medium, slow, slower, veryslow)
-fps    视频帧率,默认24帧/秒,只能是整数

```
    
输出的文件
=======

    hi.avi             # 原始视频
    .hi.avi.videoc     # 转换后目录
        _1_1.mp4       # 转换后视频
        preview.jpg    # 生成视频的预览图
        _preview.mp4   # 转换后预览视频(缩小版,默认1/4大小)
    
原始视频文件对象元数据更新
    
    {
        ...
        videoc_dir    : "id:xxx"     // .hi.avi.videoc目录的id
        video_preview : "id:xxx"     // 指出预览视频的 ID
        thumb         : "id:xxx"     // 缩略图的 ID，是 preview.mp4 的 thumb
        ...
    }
    
输出文件对象元数据更新

    {
        ...
        videoc_source : "id:xxx" // 原视频的id
        ...
    }
示例
=======

    // 默认转换
    videoc hi.avi
    
    // 带调试信息
    videoc -v hi.avi
    
    // 带调试信息且保留临时文件
    videoc -v -k hi.avi
    
    // 定制目标格式为libx265, 即h265格式
    videoc -v -vcodec libx265 hi.avi
    
    // 定制视频码流为600k,音频码流为128k
    video -v -bc 600 -ba 128 hi.avi
    
    // 定制视频转换的预设配置(可选: ultrafast,superfast, veryfast, faster, fast, medium, slow, slower, veryslow)
    video -preset medium