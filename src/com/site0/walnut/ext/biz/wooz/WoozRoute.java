package com.site0.walnut.ext.biz.wooz;

import java.util.Date;

public class WoozRoute extends AbstraceWoozPoint {
/**
        "type" : "P1",
        //............................................
        "lng": 116.197661,    // 经度
        "lat": 39.971698,     // 纬度
        "ele": 0,             // 海拔 (米)
 */
    public String type;
    public Date time;
    
    // 与上一个线路点的数据
    public double goUp;
    public double goDown;
    public double goDistance;
    
    // 累计数据
    public double countUp;
    public double countDown;
    public double countDistance;
    
    // 距离下一个CP点的数据
    public double cpUp;
    public double cpDown;
    public double cpDistance;
    public String cpName;
}
