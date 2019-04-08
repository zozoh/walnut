# 简洁

    `licence` 将对 LicenceService 做一层封装主要提供各种查询操作，以便客户端调用。
    
    本命令主要用来查看许可证信息。 权限方面的限制是这样的:
    
    - 任何域的管理员都能查看本域对于任何域提供商任何应用的许可证信息
    - 任何域的管理与都能查看本域提供的全部许可证
    - root 和 op 组不受任何限制

# 用法

    licence [-client xxxx] [-provider xxxx] [-app xxxx] [-acode]
            [-cqlnH]
            [-tihbs|ibase]
            [-tmpl]
            [-limit|skip|pager]
    
    -client    客户域名, 默认当前域
    -provider  服务商域，默认所有域
    -app       应用名称，默认所有的应用
    -acode     输出的是激活码信息。不声明的话，则输出的是许可证本身
    
    ! 注，分页信息只有在给定服务提供商的条件下才生效
    
# 示例

    获取自己在 nutz 域，关于 wn.hmaker 应用的许可证信息
    licence -provider nutz -app wn.hmaker
    
    获取自己在 nutz 域全部的激活码信息
    licence -provider nutz -acode
    
    获取自己全部的许可证信息
    licence
    
    
    
    
    