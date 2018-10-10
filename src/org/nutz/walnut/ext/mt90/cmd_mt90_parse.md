# 命令简介 

    `mt90 parse` 处理mt90的轨迹数据

# 用法

```
mt90 parse 
		[file]    # 轨迹文件的路径,必填
		[-kml]    # 输出KML格式
		[-gpx]    # 输出GPX格式
		[-name]   # 轨迹文件内部名称,默认是MT90-${time}
		[-begin XX] # 起始时间,以北京时间为准, 例如 "2018-10-05 06:00:00"
		[-end XX]   # 结束时间,以北京时间为准, 例如 "2018-10-05 12:00:00"
		[-gpsFixed] # 仅输出GPS定位成功的轨迹点,默认是false,即全部输出
		[-simple]   # 输出简单版的数据,是wooz格式的子集,并自动套用wgs84->gcj02的转换
		[-lineOnly] # 仅输出轨迹线,KML模式调试用
		[-speed 300] # 最高瞬时速度限制,默认300km/h
```

## 示例

### 解析为原始JSON数据

```
wooz:~$ mt90 parse 21KM_usera
[
{
   rtimestamp: 1538715930037, //接收该记录时的服务器时间戳
   eventKey: 35,              // 事件id
   lat: 41.212148,            // 纬度
   lng: 116.636695,           // 经度
   localtime: "181005050527", // GPS时间,通常是UTC时间
   gpsFixed: "A",             // A代表定位成功,V代表定位丢失
   satellite: 7,              // 卫星数量
   gsmRssi: 30,               // GSM信号强度
   speed: 0,                  // 瞬时速度
   direction: 96.0,           // 方向角,请无视
   locPrecision: 1.2,         // 定位经度,请无视
   ele: 751,                  // 海拔
   mileage: 102918,           // 里程,无视
   runtime: 96901,            // 运行时间,无视
   baseStation: "460|0|31AD|7720", // 基站数据,无视
   gpio: 0,                   // GPIO状态,无视
   adc: "0000|0000|0000|0921|0000", // ADC状态,其中包含电池电压等信息,后面有处理好的数据
   wall: 1,                   // 地址围栏,无视
   powerVoltage: 3765,        // 电池电压,单位毫伏
   powerQuantity: 342,        // 电池用量,好像是
   timestamp: 1538687127000,  // GPS时间戳
   recDate: "2018-10-05 13:05:30", // 服务器接收时间的字符串
   gpsDate: "2018-10-05 13:05:27"  // GPS时间的字符串(北京时间)
}
...
]
```

### 解析并输出为KML

```
wooz:~$ mt90 parse 21KM_usera -kml
<?xml version="1.0" encoding="utf-8"?>
<kml xmlns="http://earth.google.com/kml/2.1">
  <Document>
    <name>MT90-2018-09-29T05:49:18Z</name>
    <open>1</open>
    <Style id="yellowLineGreenPoly">
      <LineStyle>
        <color>7f00ffff</color>
        <width>4</width>
      </LineStyle>
    </Style>
    <Placemark>
      <name>Line</name>
      <styleUrl>#yellowLineGreenPoly</styleUrl>
      <LineString>
        <tessellate>1</tessellate>
        <altitudeMode>relativeToGround</altitudeMode>
        <coordinates>116.40548,40.030293,90
116.644411,41.092311,853
116.644385,41.092315,853
...
```

### 解析并输出为GPX

```
wooz:~$ mt90 parse 21KM_usera -kml
<?xml version="1.0" encoding="utf-8"?>
<kml xmlns="http://earth.google.com/kml/2.1">
  <Document>
    <name>MT90-2018-09-29T05:49:18Z</name>
    <open>1</open>
    <Style id="yellowLineGreenPoly">
      <LineStyle>
        <color>7f00ffff</color>
        <width>4</width>
      </LineStyle>
    </Style>
    <Placemark>
      <name>Line</name>
      <styleUrl>#yellowLineGreenPoly</styleUrl>
      <LineString>
        <tessellate>1</tessellate>
        <altitudeMode>relativeToGround</altitudeMode>
        <coordinates>116.40548,40.030293,90
116.644411,41.092311,853
116.644385,41.092315,853
...
```