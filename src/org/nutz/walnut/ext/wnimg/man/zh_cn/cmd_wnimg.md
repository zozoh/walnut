# 命令简介 

    `wnimg` 命令用来管理一个图片
    
主要用途:
1. 缩放(scale)
2. 旋转(route)
3. 裁剪(cut)
4. 镜像(mirror)

# 用法

```
wnimg [abc.jpg] [-filter 'scale(0.5) cover(1920,1080)'] [-qa 0.8] [-out out.jpg]
```

* [abc.jpg] 原文件,如果没有,从流读取
* filter 过滤器及配置, 空格间隔

#示例


1. 缩小到原本的1/2, 以封面模式缩放为1920x1080, 输出品质0.6f, 目标文件xyz.jpg
```
wnimg abc.jpg -filter 'scale(0.5) cover(1920,1080) ' -qa 0.6 -out  xyz.jpg
```

2. x轴缩放到0.7, y轴缩放到0.5, 输出品质0.8f, 目标文件xyz.jpg
```
wnimg abc.jpg -filter 'scale(0.7, 0.5)' -qa 0.8 -out  xyz.jpg
```

3. 裁剪(起点坐标0x0, 区域120x180), 输出品质0.8f, 目标文件xyz.jpg
```
wnimg abc.jpg -filter cut(0,0,120,180)' -qa 0.8 -out  xyz.jpg
```

4. 裁剪(起点坐标相对位置0.3x0.3, 区域120x180), 输出品质0.8f, 目标文件xyz.jpg
```
wnimg abc.jpg -filter cut(0.3,0.3,120,180)' -qa 0.8 -out  xyz.jpg
```

5. 左右镜像(0), 上下翻转(1)
```
wnimg abc.jpg -filter mirror(0)' -qa 0.8 -out  xyz.jpg
```