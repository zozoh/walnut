命令简介
======= 

`domain site` 获取与当前域登录设置有关的信息
    

用法
=======

```bash
domain site
    [id:xxx]         # 【选】站点目录路径
    [-key %PID]      # 输出的字段过滤条件
    [-cqn]           # JSON 输出的格式化方式
```

示例
=======

```bash
# 输出当前票据对应的会话信息
www checkme id:xxx f6d..4ace

# 输出当前票据对应的会话信息的 AjaxReturn 形式
www checkme id:xxx f6d..4ace -ajax

# 更新当前票据对应的账户元数据，并输出会话信息
echo 'sex:2' | www checkme id:xxx f6d..4ace -u
```