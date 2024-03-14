命令简介
======= 

`oauth callback` 命令校验第三方登录的结果

    
用法
=======

```    
oauth callback [id]   # 第三方登录的ID,例如github,qq
```

需要从标准输入读取第三方返回的全部参数,但通常只有code

示例
=======

```
echo "{code:'3798ee27cc0578573154'}" | oauth callback github
{
   provider: "github",
   profileId: "589819",
   headimgurl: "https://avatars0.githubusercontent.com/u/589819?v=3",
   aa: "wendal"
}
```