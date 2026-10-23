# 命令简介

`wget` 下载文件（HTTP/HTTPS）

默认按 URL 末段命名保存到当前目录；`-O` 指定目标路径。

# 用法

```bash
wget [-O 目标路径] URL
```

# 示例

```bash
# 下载，默认文件名
wget http://nutzam.com/index.html

# 指定文件名
wget -O qq.html http://nutzam.com/index.html
```
