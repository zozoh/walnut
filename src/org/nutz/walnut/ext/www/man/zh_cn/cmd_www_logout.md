命令简介
======= 

`www signout` 注销会话
    

用法
=======

```bash
www signout
    [id:xxx]      # 【必须】站点主目录路径
    [-ticket xxx] # 【必须】表示当前已登录的会话的票据
    [-cqn]        # JSON 输出的格式化方式
    [-ajax]       # 开启这个选项，则输出为 ajaxReturn 的包裹
```

示例
=======

```bash
# 注销
www signout id:xxx -ticket f6d..4ace 

# 注销（输出 AjaxReturn）
www signout id:xxx -ticket f6d..4ace -ajax 
```