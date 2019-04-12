# 命令简介 

    `mt90 eta` 根据选手轨迹,预估到达时间,同时进行路线匹配

# 用法

```
mt90 eta 
		[...] 支持parse命令的所有参数
		[-map xxx] 地图数据,必填
		[-all] 预测所有轨迹点的到达时间,调试用
		[-debug] 打印完整的调试信息
```

## 示例

### 解析轨迹数据并输出图片

```
wooz:~$ mt90 eta 70KM_x234fdf -map mars_google.json -image 70KM_x234fdf.jpg
{
	"eta_cp":3245,               // 预计到达下一个CP点的耗时 
	"eta_cp_time":1540669865000, // 预计到达下一个CP点的北京时间 
	"eta_end":5743,              // 预计到达终点的耗时 
	"eta_end_time":1540669865000 // 预计到达终点的北京时间 
}
```