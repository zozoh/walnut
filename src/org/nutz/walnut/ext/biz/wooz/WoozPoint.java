package org.nutz.walnut.ext.biz.wooz;

public class WoozPoint extends AbstraceWoozPoint {
/**
        "name" : "P1",          // 赛点名称，整个文档中必须唯一
        "desc" : "起点",        // 赛点的显示文字，一般为地名的别名
        赛点的类型
          - C : 打卡
          - D : 饮料
          - F : 食物
          - M : 医疗
          - T : 厕所
          - W : 换装
        类型字符串可以多选，譬如 
          CD : 表示打卡+饮料
          CDMFTW : 表示全部功能
        类型字符必须从小到大排序
        "type" : "CDFMTW",
        //............................................
        "lng": 116.197661,    // 经度
        "lat": 39.971698,     // 纬度
        "ele": 0,             // 海拔 (米)
        //............................................
        "distanceStart" : 100344,   // 距离起点点的里程（米）
        "distanceEnd" : 23032,      // 距离终点的里程（米）
        "distancePrev" : 14200,     // 距离上一个赛点的里程（米）
        "distanceNext" : 12300,     // 距离下一个赛点的里程（米）
        //"eleNext" : 13.4,           // 距离下一个赛点的海拔差（米）
        //............................................
        "closeAt" : "2018-11-24T12:23:23"  // 赛点关门时间
 */
    public String name;
    public String desc;
    public String type;
    public String service;
    public int distanceStart;
    public int distanceEnd;
    public int distancePrev;
    public int distanceNext;
    //public int eleNext;
    public String closeAt;
    
    public int routePointIndex; // 关联的轨迹点索引
    public int routePointDistance;  // 与关联的轨迹点的距离,调试用
    
    public int goUp; // 上一个赛点到本赛点的净抬升
    public int goDown; // 上一个赛点到本赛点的净下降
    public int nextUp; // 距离下一个赛点的净抬升
    public int nextDown; // 距离下一个赛点的净下降
    
    
    public int getRoutePointIndex() {
        return routePointIndex;
    }
    public void setRoutePointIndex(int routePointIndex) {
        this.routePointIndex = routePointIndex;
    }
    
    
}