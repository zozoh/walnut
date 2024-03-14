命令简介
======= 

`www checkme` 查看当前会话信息（可同时修改会话账户元数据）
    

用法
=======

```bash
www checkme
    [id:xxx]         # 【必须】站点主目录路径
    [$ticket]        # 【必须】用户登录票据
    [-cqn]           # JSON 输出的格式化方式
    [-ajax]          # 开启这个选项，则输出为 ajaxReturn 的包裹
    [-u [{..}]]      # 更新当前会话账户的元数据
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