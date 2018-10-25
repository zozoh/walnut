package org.nutz.walnut.ext.wooz;

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
    public double goUp;
    public double goDown;
    public double goDistance;
}
