# 命令简介 

`ti sidebar` 用来获取当前会话的侧边栏信息。

> [侧边栏文档](https://github.com/zozoh/titanium/blob/master/doc/en-us/walnut/sidebar.md)

-------------------------------------------------------------
# 用法
 
```bash
ti sidebar 
  [/path/to/sidebar.json] # [选]直接指定侧边栏文件的路径
  [-cqn]                  # [选]JSON 输出的格式化   
```

- 对象路径与 `-name` 必须要给定一个，否则本命令不知道要输出什么
- 如果不指定路径，本命令会查找环境变量 `SIDEBAR_PATH` 依次查看侧边栏文件
- `SIDEBAR_PATH` 的值是半角冒号分隔的侧边栏文件路径，
    - 例如  "~/.ti/sidebar.json:/rs/ti/view/sidebar.json"

-------------------------------------------------------------
# 示例

```bash
# 获取当前会话侧边栏
demo:~$ ti sidebar

# 获取指定侧边栏信息
demo:~$ ti sidebar ~/mysidebar.json
```