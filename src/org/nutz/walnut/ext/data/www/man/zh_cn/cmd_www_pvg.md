# 命令简介 

`www pvg` 获取当前会话（域用户）特殊的角色权限设定

-------------------------------------------------------------
# 用法
 
```bash
www pvg [-cqn]    # [选]JSON 输出的格式化   
```

-------------------------------------------------------------
# 示例

```bash
# 获取当前会话（域用户）特殊的角色权限设定
demo:~$ www pvg
{
   action1 : true,
   createFile : false
   ...
}
```