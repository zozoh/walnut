---
title: 业务权限模型
author: zozohtnt@gmail.com
tags:
- 概念
- 权限
- 高阶
- 业务
---

--------------------------------------
# 动机：为什么要有业务权限模型

**Walnut**的[基础权限模型][c0-bpm]虽然非常通用和灵活，但是正因为通用和灵活，在具体的业务应用上，如果要想设置的非常贴合业务，必然是非常繁琐的。如果业务足够复杂，采用[基础权限模型][c0-bpm]来描述权限，甚至是不可能的。

因此，我们希望能有一个权限模型，针对业务来划分动作和角色，只需要简单的勾选即可完成权限的设定：

 Roles | Action-A | Action-B | Action-C | Action-D | Action-E 
-------|----------|----------|----------|----------|----------
 Role0 | Yes      | --       | --       | --       | --    
 Role1 | Yes      | --       | Yes      | Yes      | --    
 Role2 | Yes      | Yes      | --       | --       | --    
 Role3 | Yes      | Yes      | Yes      | Yes      | Yes  
 Role4 | Yes      | --       | Yes      | Yes      | Yes

--------------------------------------
# 计划应用场景

这种业务模型，可以被应用到下面的具体模块场景中，譬如：

- HttpApi 的访问控制
- WWW 页面的选择性加载或者跳转
- Wn.Manger 界面的`菜单/侧边栏/主界面`等的选择性加载
- 后台程序
- 钩子程序

--------------------------------------
# 设计思路与边界

因为应对几乎是各种场景，这个模型与[基础权限模型][c0-bpm]最大的不同是：

**它必须是被动的**

即，[基础权限模型][c0-bpm]守卫的是数据安全性的底线，因此它在 IO 层之下对所有
业务几乎是透明的（除非特意声明内核态），而**业务权限模型**为了考虑更广阔的应用场景和使用方式，它必须是被动式的等待调用者来询问。

为此，这个模型，我们只能添加下面几点约束：

1. 描述权限的数据格式是约定好的，但是来源不限，但通常是来自一个元数据或者文件内容
2. 调用的场景只能在服务或者命令脚本里

为此，**业务权限模型**将给出：

- 描述权限的数据格式规范
- 一个服务类： `WnPvgMoreService`
- 一个封装命令: `cmd_pvg`

--------------------------------------
# 数据结构描述

权限的描述来自一个 JSON 数据：

```json
{
  "Role1" : {
    "Action-A" : true,
    "Action-B" : true,
    "Action-C" : false,
    "Action-D" : false,
    "Action-E" : false,
  }
}
```

当然你可以将这种形式的数据封装为任何形式，只要最后能恢复回来交给业务模型即可。

> 虽然并不强制权限数的存放方式，但是我们推荐你可以把你的权限数据存放到 
> `~/.pvg/我的业务权限模型.json` 路径下

--------------------------------------
# 使用方式

在服务类中调用:

```java
// 是否具备某个行为的权限
pvgMoreService.can("Role1", "Action-B")

// 是否全部具备某几个行为的权限
pvgMoreService.can("Role1", "Action-B", "Action-C")

// 是否具备某几个行为中至少一个的权限
pvgMoreService.canOr("Role1", "Action-B", "Action-C")
```

在命令行中调用:

```bash
# 是否具备某个行为的权限
demo:> pvg ~/my-pvg.json can Role1 Action-B
true

# 是否全部具备某几个行为的权限
demo:> pvg ~/my-pvg.json can Role1 Action-B Action-C
false

# 是否具备某几个行为中至少一个的权限
demo:> pvg ~/my-pvg.json can -or Role1 Action-B Action-C
true

# 返回结果用 Ajax 格式包裹
demo:> pvg ~/my-pvg.json can Role1 Action-B Action-C -ajax -cqn
{"ok":true,"data":{"Action-B":true,"Action-C":true}}

# 返回结果用 Ajax 格式包裹
demo:> pvg ~/my-pvg.json can Role1 Action-B Action-E -ajax -cqn
{"ok":false,"data":{"Action-B":true,"Action-E":false}}
```

--------------------------------------
# 相关知识点

- [基础权限模型][c0-bpm]

[c0-bpm]: ../core-l0/c0-basic-privilege-model.md