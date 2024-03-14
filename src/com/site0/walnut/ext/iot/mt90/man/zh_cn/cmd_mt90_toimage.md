# 命令简介 

    `mt90 toimage` 处理mt90的轨迹数据并绘制成图片

# 用法

```
mt90 toimage 
		[...] 支持parse命令的所有参数
		[-image xxx.jpg] 输出的图片路径,必须填写 
```

## 示例

### 解析轨迹数据并输出图片

```
wooz:~$ mt90 toimage 70KM_x234fdf -map mars_google.json -image 70KM_x234fdf.jpg
```