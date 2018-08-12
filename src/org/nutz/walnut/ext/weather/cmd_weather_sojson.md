# 命令简介 

    `weather sojson` 通过城市名, 走sojson获取天气数据

用法
=======

```
-> weather sojson 北京
{
   date: "20180812",
   message: "Success !",
   status: 200,
   city: "北京",
   count: 1,
   data: {
      shidu: "88%", // 当前湿度
      pm25: 42.0, // PM2.5浓度
      pm10: 18.0, // PM10浓度
      quality: "良", // 空气质量级别
      wendu: "25", //当前温度
      ganmao: "极少数敏感人群应减少户外活动",
      // yesterday是昨天的温度
      yesterday: {
         date: "11日星期六",
         sunrise: "05:20",
         high: "高温 31.0℃",
         low: "低温 23.0℃",
         sunset: "19:18",
         aqi: 98.0,
         fx: "东南风",
         fl: "<3级",
         type: "雷阵雨",
         notice: "带好雨具，别在树下躲雨"
      },
      // forecast是今后几天的预报
      forecast: [{
         date: "12日星期日", // 时间
         sunrise: "05:21", // 日出时间
         high: "高温 30.0℃", // 最高温度
         low: "低温 24.0℃",  // 最低温度
         sunset: "19:16",   // 日落时间
         aqi: 77.0,
         fx: "东南风", // 风向
         fl: "<3级", // 风力级别
         type: "雷阵雨", // 天气类型
         notice: "带好雨具，别在树下躲雨" // 温馨提示
      },
      // --- 后面几天的预报
      ]
   }
}
```