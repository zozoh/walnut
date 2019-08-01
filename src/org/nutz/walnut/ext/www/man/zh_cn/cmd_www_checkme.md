命令简介
======= 

`www checkme` 获取当前会话信息
    

用法
=======

```bash
www checkme
    [id:xxx]         # 【必须】站点主目录路径
    [$ticket]        # 【必须】用户登录票据
    [-cqn]           # JSON 输出的格式化方式
    [-ajax]          # 开启这个选项，则输出为 ajaxReturn 的包裹
```

示例
=======

```bash
# 输出当前票据对应的会话信息
www checkme id:xxx f6d..4ace

# 输出当前票据对应的会话信息的 AjaxReturn 形式
www checkme id:xxx f6d..4ace -ajax
```