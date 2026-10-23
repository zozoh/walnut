# 命令简介

`chimg` 转换图片：缩放、旋转、修正方向

处理流程：先旋转（`-ro`），再缩放（`-s`）。输出默认覆盖原文件，
也可用 `-o` 指定新位置。处理后会更新对象的 `width`/`height` 元数据。

# 用法

```bash
chimg 源路径 [目标路径] [-s 宽x高] [-z] [-bg 颜色] [-ro 角度] [-iosfix]
  -s       # 缩放到指定尺寸（如 800x600）；不带 -z 为剪裁缩放
  -z       # 保持原比例缩放（不足部分用背景色填充）
  -bg      # 配合 -z 指定背景色（如 #ffffff）
  -ro      # 旋转角度：90 顺时针、-90 逆时针、0 不旋转
  -o       # 指定输出位置（优先于目标路径参数）
  -iosfix  # 按 EXIF 方向自动修正 iOS 设备拍照的旋转问题
```

# 示例

```bash
# 将图片修改为 800x600
chimg abc.jpg -s 800x600

# 输出到指定位置
chimg abc.jpg -s 800x600 -o /root/abc_800x600.jpg

# 保持原比例，背景白色
chimg abc.jpg -z -bg #ffffff -s 800x600

# 顺时针旋转 90 度
chimg abc.jpg -ro 90

# 修正 iPhone 照片方向
chimg iphone.jpg -iosfix -o fixed.jpg
```
