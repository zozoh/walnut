# 命令简介

    `ticket my` 我的工单，提供用户与客服跨域与工单服务交互的能力

# 重要说明

    下文中${id}为walnut用户id，可通过命令`me id`查看
    
    个人记录文件位置为 ~/.ticket_my.json
    
    ```js
    {
       "s": "127.0.0.1:9599",
       "tp": "user",
       "ts": "ts001",
       "tsNm": "walnut工单系统",
       "confs" : {
           "工单服务xxx": {....},
           "工单服务yyy": {....},
           "工单服务zzz": {....}
       }
    }
    ```
    
    其中confs中记录了所有已注册的工单服务
    
    注册/切换配置后，s,tp,ts等默认值会自动对应填充，无需再每次执行命令时指定。
    
# 用法

    ```
    ticket my
        [-ts xxxxx]                     # 工单服务域名，默认为当前域
        [-tp user|cservice]             # 人员类型，默认为user
        [-reg]                          # 将用户注册到工单系统
        [-s 127.0.0.1]                  # 服务器地址，默认为本机
        [-conf xxxx]                    # 本地配置，记录注册过的工单服务
        [-post '工单id']                 # 提交工单，带参数表示回复某工单，不带参数表示提交新工单
        [-c 'text: "问题描述"']           # 提交内容，放到text属性中
        [-atta xxxx,xxxx]               # 附件文件id，仅支持walnut系统上的文件id，否者直接从流中读取文件内容
        [-i 0]                          # 记录的序号，从0开始，不填则默认添加到最后一次提交中
        [-list 'tp: new']               # 查询我的工单，可添加过滤条件
        [-skip 0 -limit 10]             # 查询数量与范围限制，默认返回最近更新10条
        [-fetch '工单id']                # 获取工单详细信息
        [-assign '工单id' -tu xxx]       # 获取工单详细信息
    ```

# 示例

    将我注册到site0下，以用户的身份
    ticket my -reg -ts site0 
    
    将我注册到site0下，以客服的身份
    ticket my -reg -ts site0 -tp cservice
    
    将当前域用户注册为用户，测试机端口为9599
    ticket my -reg -s 127.0.0.1:9599
    
    查看我的当前配置，配置项中为各个参数的默认值
    ticket my -conf
    
    查看我注册过的工单服务列表
    ticket my -conf list
    
    切换到指定的工单服务，xxx不能用list关键字
    ticket my -conf xxx
    
    查询我的工单(全部)
    ticket my -list
    
    查询我的工单(按照状态)
    ticket my -list 'ticketStatus: "new"'
    
    获取工单详细内容
    ticket my -fetch '${工单id}'
    
    提交新工单
    ticket my -post -c "text: '内容'"
    
    回复工单
    ticket my -post '${工单id}' -c "text: '内容'"

    提交新工单(带附件)
    ticket my -post -c "text: '内容'" -atta xxxx,xxxx
    
    仅提交附件，加在最后一次请求上
    ticket my -post '${工单id}' -atta xxxx,xxxx
    
    仅提交附件，加在第一次请求上
    ticket my -post '${工单id}' -atta xxxx,xxxx -i 0
    
    确定工单已解决
    ticket my -post '${工单id}' -c "text: '内容', finish: true"
    
    
    