# 功能扩展

`Walnut` 除了提供的一整套存储、权鉴、命令系统，还在这些基础上提供了一系列开箱即用的功能。
它们包括:

- 发送和接收 Email
- 发送 SMS 的能力
- 解析 word/excel 等 Office 文档
- 根据模版渲染 word 文档
- 输出 csv 文件
- 图片的基本处理
- 获取视频基本信息以及封图
- 对于二维码的支持

这些功能有一个共同的特点：**它们都是以命令的方式提供的**。

因为 `Walnut` 里任何业务逻辑都可以写成一条命令，
所以这些功能可以立刻被拼接进你的业务命令里——
定时任务、HTTP API、钩子、JS 脚本，任何地方都能直接调用。
你不需要引入额外的 SDK，也不必关心它们底层的 Java 实现。

```text
70.functionate/
|-- README.md            # 本篇
|-- 10-email-mailx.md    # Email 收发（mailx）
|-- 20-sms.md            # SMS 短信（占位，待重写）
|-- 30-office-ooml.md    # Office 文档解析与模板渲染（ooml）
|-- 40-sheet-csv.md      # 表格与 CSV 输出（sheet）
|-- 50-image-imagic.md   # 图片处理（imagic）
|-- 60-video-ffmpeg.md   # 视频信息与封图（videoi / videoc）
|-- 70-qrcode.md         # 二维码（qrcode）
|-- 80-lock-api.md       # 锁服务（lock）：互斥控制与防重入
```

每篇文档都遵循同样的路子：先讲这个命令解决什么问题，
再给最少可用的例子，最后把常用用法铺开。
命令的全部子命令手册（man 文档）随源码存放在
`src/com/site0/walnut/ext/<包名>/man/zh_cn/` 下，需要细节时去那里查。
