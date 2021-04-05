命令简介
======= 

`www` 用来处理用户域名映射和发布目录的相关操作
    

用法
=======

```bash
www query       # 【废弃】查询域名映射的信息列表
www add         # 【废弃】添加一个新的域名映射
www rm          # 【废弃】移除一个域名映射
www renew       # 【废弃】处理域名的续费成功的后续操作
www conf        # 【废弃】得到域名映射方面的配置文件信息(JSON)

www captcha      # 验证码
www checkme      # 查看当前会话信息（可同时修改会话账户元数据）
www account      # 查看指定用户信息（可同时修改元数据）
www auth         # 登录/绑定手机/注册
www passwd       # 修改账户密码
www logout       # 注销会话 
www buy          # 创建购买商品的订单（可自动创建支付单）
www order        # 获取指定用户的某份订单详情
www price        # 计算订单价格（包括运费，优惠等）
www pay          # 根据订单创建支付单
www payafter     # 支付成功后，对于订单的后续处理
www avatar       # 为某账户设置头像
www updateprice  # 根据调整后的订单基础金额和运费设置订单费用
www pvg          # 获取当前会话（域用户）特殊的角色权限设定
```
