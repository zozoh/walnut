# 命令简介 

    `weather gaode` 通过城市名, 走gaode获取天气数据

用法
=======

weather gaode [城市Code或者城市名] -tp <类型> -k <密钥> -cqn

-tp 数据类型, base实时天气, all预测天气
-k 密钥, 默认使用内置密钥

```
-> weather gaode 北京
{
   status: "1",
   count: "1",
   info: "OK",
   infocode: "10000",
   lives: [{
      province: "北京",
      city: "北京市",
      adcode: "110000",
      weather: "晴",
      temperature: "-1",
      winddirection: "西南",
      windpower: "≤3",
      humidity: "17",
      reporttime: "2020-12-14 12:31:02"
   }]
}
```