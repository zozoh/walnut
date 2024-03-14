# 命令简介 

`iimg` 命令将输出一个图片文件的信息，同时也可以为图片生成缩略图

- 本命令会自动为图片文件增加 "width" 和 "height" 这两个元数据
- 如果不是一个图片文件，本命令将抛错

# 用法

    iimg $FILE              # 图片文件 
         [-thumb 64x64]     # 是否生成缩略图
         [-thumbta id:xxx]  # 要生成缩略图的目标文件对象，默认为当前图片
         [-mode zoom|clip]  # 剪裁模式，默认 zoom
         [-force]           # 是否强制重新计算，否则会自动寻找一个现成的计算结果
         [-bgc]             # 如果 mode==zoom，可支持背景色，支持 RRGGBB 格式
         [-Q]               # 不输出，否则将输出对象的 JSON        
         [-c|n|q|l]         # 如果输出的话，JSON 的格式
         [-compress]        # 是否压缩,这影响到dst/quality/width/height 是否生效
         [-dst XXX]         # 压缩后的存放路径,若不指定,则原地压缩
         [-quality 0.9]     # 压缩级别,默认是0.9f
         [-width 1920]      # 最大宽,优先级大于"高"
         [-height 1080]     # 最大高, 若宽高均不指定,则输出原来的大小 
         
    
# 示例

    // 生成 64x64 大小的缩略图，并输出图片信息
    iimg ~/myphoto.jpg -thumb 64x64 
    
    // 原地压缩
    iimg ~/myphoto.jpg -compress
    
    // 指定压缩质量
    iimg ~/myphoto.jpg -compress -quality 0.9
    
    // 指定输出目标
    iimg ~/myphoto.jpg -compress -dst myphoto_08f.jpg