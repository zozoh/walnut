命令简介
======= 

`www logout` 注销会话
    

用法
=======

```bash
www logout
    [id:xxx]      # 【必须】站点主目录路径
    [-ticket xxx] # 【必须】表示当前已登录的会话的票据
    [-cqn]        # JSON 输出的格式化方式
    [-ajax]       # 开启这个选项，则输出为 ajaxReturn 的包裹
```

示例
=======

```bash
# 注销
www logout id:xxx -ticket f6d..4ace 

# 注销（输出 AjaxReturn）
www logout id:xxx -ticket f6d..4ace -ajax 
```