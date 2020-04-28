# 命令简介 

`ti www` 用来发布一个站点。

-------------------------------------------------------------
# 用法
 
```bash
ti www 
  [/path/to/src]      # 站点的工程目录
  [/path/to/dist]     # 站点的输出目录
  [-cdn http://xx]    # 覆盖站点全局配置的 `cdnBase` 的设置
  [-rs /gu/rs]        # 静态资源链接路径
  [-base /www/abc/]   # 站点可访问主路径
  [-wnml]             # 预先渲染 index.wnml
```

-------------------------------------------------------------
# 示例

```bash
# 将工程目录发布到指定目录
demo:~$ ti www ~/site/workspace ~/www

# 将工程目录发布到指定目录并预先渲染 wnml
demo:~$ ti www ~/site/workspace ~/www -wnml
```