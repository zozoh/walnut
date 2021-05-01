命令简介
======= 

`www account` 查看指定用户信息（可同时修改元数据）
    

用法
=======

```bash
www account
    [id:xxx]         # 【必须】站点主目录路径
    [User]           # 【选】目标用户的ID或登录名或手机或邮箱
                     # 或者可以是一个 `{...}` 形式的查询条件 
　　［-list]          # 列表模式输出
    [-limit 50]      # 列表模式下，最多输出多少条记录
    [-skip 0]        # 列表模式下，跳过多少条记录才开始输出
    [-sort {..}]     # 列表模式下，排序方式
    [-cqn]           # JSON 输出的格式化方式
    [-ajax]          # 开启这个选项，则输出为 ajaxReturn 的包裹
    [-u [{..}]]      # 更新账户的元数据
```

示例
=======

```bash
# 输出账户信息
www account id:xxx xiaobai

# 输出账户信息的 AjaxReturn 形式
www account id:xxx xiaobai -ajax

# 更新账户元数据，并输出
echo 'sex:2' | www account id:xxx xiaobai -u
```