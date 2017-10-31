# 命令简介

    `ticket recored` 管理工单记录

# 用法


    ```
    ticket record
        [-u ${uid}]              # 查询用户id
        [-ts xxxxx]             # 工单服务域名，默认为当前域
        [-tp user|cservice]     # 人员类型，默认为cservice
        [-search 'x:1']         # 全局查询工单
        [-query 'x:1']          # 查询某个用户相关工单 与-u配合
        [-new]                  # 新建工单，与-c配合
        [-c 'text:"问题"']       # 提交/回复内容
        [-assign ${id}]         # 工单ID 分配到客服 与-tu配合
        [-tu ${uid}]             # 客服ID
        [-fetch ${id}]          # 获取指定工单内容
        [-reply ${id}]          # 回复工单，与-c配合
    ```

# 示例

    