package org.nutz.walnut.ext.mt90.bean;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.nutz.castor.Castors;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;

public class Mt90Raw implements Comparable<Mt90Raw> {

    public long rtimestamp; // 时间戳
    public int eventKey; // 事件ID
    public double lat; // 纬度
    public double lng; // 经度
    public String localtime; // 设备本地时间
    public String gpsFixed; // A 代表定位成功
    public int satellite; // 卫星数量
    public int gsmRssi; // GSM信号强度 0-31
    public int speed; // 速度
    public double direction; // 方向
    public double locPrecision; //定位精度
    public int ele; // 海拔
    public int mileage; // 行驶里程
    public int runtime; // 运行时间
    public String baseStation; // 基站信息
    public int gpio; // GPIO端口状态
    public String adc; // 模拟输入ADC
    public int wall; // 地址围栏
    
    // 计算得出的量
    public int powerVoltage; // 单位毫伏
    public int powerQuantity; // 单位0.01%
    public long timestamp;
    public Date recDate;
    
    private static Mirror<Mt90Raw> mirror = Mirror.me(Mt90Raw.class);
    private static Field[] fields = Mt90Raw.class.getDeclaredFields();
    
    public static Mt90Raw mapping(String line) {
        Mt90Raw raw = new Mt90Raw();
        String[] tmp = line.trim().split(",");
        for (int i = 0; i < tmp.length; i++) {
            //System.out.println(fields[i].getName() + "=" + tmp[i]);
            mirror.setValue(raw, fields[i], Castors.me().castTo(tmp[i], fields[i].getType()));
        }
        if (!Strings.isBlank(raw.adc)) {
            tmp = raw.adc.split("\\|");
            if (tmp.length > 4) {
                int AD4 = Integer.parseInt(tmp[3], 16);
                //System.out.println("AD4="+AD4);
                raw.powerVoltage = (int)((AD4*3.3*2)/4096*1000);
                raw.powerQuantity = (int)((AD4*3.0*2/4096)*100);
            }
        }
        raw.recDate = new Date(raw.rtimestamp);
        if (!Strings.isBlank(raw.localtime)) {
            try {
                raw.timestamp = new SimpleDateFormat("yyyyMMddHHmmss").parse("20"+raw.localtime).getTime() + 8*3600*1000;
            }
            catch (ParseException e) {
            }
        }
        return raw;
    }

    public int compareTo(Mt90Raw o) {
        if (timestamp > o.timestamp)
            return -1;
        else if (timestamp < o.timestamp)
            return 1;
        return 0;
    }
    
//    public static void main(String[] args) {
//        Mt90Raw raw = mapping("1535273067692,35,25.294176,110.294135,180826084426,A,6,27,0,254,1.2,167,482672,102514,460|0|77C7|8EEF,0000,0000|0000|0000|0867|0000,00000001");
//        System.out.println(Json.toJson(raw));
//    }
}
