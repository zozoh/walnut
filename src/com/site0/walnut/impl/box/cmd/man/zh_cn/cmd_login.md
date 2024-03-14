# 命令简介 

`login` 命令将会在当前会话下创建一个任何用户的子会话。

- root 组管理员能登录到除了 root 组管理员之外任何账户
- 执行操作的用户必须为 root|op 组成员
- 目标用户必须不能为 root|op 组成员
- 域管理员只能登录自己的子账号
     
# 用法

```bash
login 
[xiaobai]  # 目标用户名
[-cqn]     # 按json输出的格式胡方式
[-H]       # 按json输出时，也显示双下划线开头的隐藏字段
[-site]    # 表示域子账号登录，可以通过这个指定站点
           #  - "id:xxx" 表示站点对象的 ID
           #  - "host:xxx.xx  表示站点对象的域名
           #  - "~/xxx"  表示站点的路径
           #  - "true" 表示采用系统默认配置，通常为 ~/www/login
           # 默认为 ~/www/login
```
    
# 示例

```bash
# 在当前会话下创建一个属于用户 xiaobai 的会话
login xiaobai

# 采用子账号登录
login xiaobai -site 

```
     
    
