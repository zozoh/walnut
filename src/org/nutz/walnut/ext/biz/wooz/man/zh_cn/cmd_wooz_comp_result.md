# 命令简介 

`wooz comp_result` 根据赛事打卡信息,计算赛事数据

同时由于考虑到只有`主办方`才有打卡数据，因此如果传入的是仙踪域赛事，会自动寻找主办方域赛事

用法
=======

```
wooz comp_result [主办方or仙踪域赛事ID]
    -write                 # 写入赛事的result目录
    -json                  # 打印json形式赛事结果
```