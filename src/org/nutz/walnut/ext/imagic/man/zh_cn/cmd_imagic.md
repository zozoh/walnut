# 命令简介 

    `imagic` 命令用来管理一个图片
    
主要过滤器:
1. 等比缩放--scale(float[,float]),scale(int[,int])
2. 旋转--rotate(int{0~360})
3. 裁剪--clip(int,int,int,int),clip(float,float,int,int)
4. 镜像--flip('h'),flip('v')
5. 根据exif旋转--autoexif(false),autoexif(true)
6. 缩放并填满--cover(int,int)
7. 缩放并放入--contains(int,int)

# 用法

```
imagic [abc.jpg] [-filter 'scale(0.5) cover(1920,1080)'] [-qa 0.8] [-out out.jpg] [-thumb '...'] [-thumbout ~/.xxx]
```

* abc.jpg 源文件,如果没有,从标准输入流读取. 如果是http://或者https://开头,则作为URL读取
* filter 过滤器及配置, 空格间隔
* qa 输出品质,默认0.8
* out 输出路径,不设置就是标准输出,设置为inplace就是原图替换
* thumb 输出图片后,再额外生成缩略图
* thumbout 的输出路径,可选

# 示例


1. 缩小到原本的1/2, 以封面模式缩放为1920x1080, 输出品质0.6f, 目标文件xyz.jpg
```
imagic abc.jpg -filter 'scale(0.5) cover(1920,1080) ' -qa 0.6 -out  xyz.jpg
```

2. x轴缩放到0.7, y轴缩放到0.5, 输出品质0.8f, 目标文件xyz.jpg
```
imagic abc.jpg -filter 'scale(0.7, 0.5)' -qa 0.8 -out  xyz.jpg
```

3. 裁剪(起点坐标0x0, 区域120x180), 输出品质0.8f, 目标文件xyz.jpg
```
imagic abc.jpg -filter 'clip(0,0,120,180)' -qa 0.8 -out  xyz.jpg
```

4. 裁剪(起点坐标相对位置0.3x0.3, 区域120x180), 输出品质0.8f, 目标文件xyz.jpg
```
imagic abc.jpg -filter 'clip(0.3,0.3,120,180)' -qa 0.8 -out  xyz.jpg
```

5. 左右镜像(0), 上下翻转(1)
```
imagic abc.jpg -filter 'mirror(0)' -qa 0.8 -out  xyz.jpg
```

6. 上下镜像,并关闭exif自动旋转
```
imagic abc.jpg -filter 'mirror(1) autoexif(false)' -qa 0.8 -out  xyz.jpg
```