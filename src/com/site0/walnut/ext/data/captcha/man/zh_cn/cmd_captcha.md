# 命令简介

`captcha` 生成图形验证码，默认输出 JSON（文字 + Base64 编码的 PNG），也可以直接输出 PNG 字节。

# 用法

```bash
captcha [-w 宽] [-h 高] [-text 答案] [-textlen 长度] [-out png] [-bg] [-noise] [-border] [-fisheye]
  -w         # 图片宽度，默认 120
  -h         # 图片高度，默认 30
  -text      # 指定验证码文字；不给则随机生成数字
  -textlen   # 随机文字的长度，默认 4
  -out       # 输出形式：json（默认）或 png（直接输出 PNG 字节）
  -bg        # 绘制背景
  -noise     # 绘制噪点
  -border    # 绘制边框
  -fisheye   # 绘制鱼眼变形
```

# 示例

```bash
# 生成验证码，JSON 输出（text + Base64 PNG）
captcha

# 指定宽高和答案
captcha -w 100 -h 40 -text 1234

# 直接输出 PNG 字节（存成文件）
captcha -w 100 -h 40 -textlen 6 -out png > ~/captcha.png

# 带噪点和边框
captcha -noise -border
```
