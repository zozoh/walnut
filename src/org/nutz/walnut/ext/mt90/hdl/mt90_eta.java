package org.nutz.walnut.ext.mt90.hdl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.mt90.Mt90Map;
import org.nutz.walnut.ext.mt90.bean.Mt90Raw;
import org.nutz.walnut.ext.mt90.bean.PolynomialSoluter;
import org.nutz.walnut.ext.mt90.bean.ReportImageBuilder;
import org.nutz.walnut.ext.wooz.WoozMap;
import org.nutz.walnut.ext.wooz.WoozRoute;
import org.nutz.walnut.ext.wooz.WoozTools;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value="cqn", regex="^(kml|gpx|gpsFixed|simple|lineOnly|debug|tomap|all|precise)$")
public class mt90_eta extends mt90_parse {
    
    private static final Log log = Logs.get();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        hc.params.setv("simple", true);
        WoozMap map = Mt90Map.get(sys.io, Wn.normalizeFullPath(hc.params.check("map"), sys));
        boolean debug = hc.params.is("debug");
        boolean precise = hc.params.is("precise"); // 精确计算球体距离,慢很多
        List<Mt90Raw> list = parse(sys, hc);
        List<WoozRoute> routes = map.route;
        NutMap re = new NutMap();
        
        Stopwatch sw = Stopwatch.begin();
        // =================================轨迹匹配============================================
        {
            // 剔除所有离线路太远的点
            int maxAway = hc.params.getInt("maxAway", 150);
            List<Mt90Raw> newList = new ArrayList<>();
            for (Mt90Raw raw : list) {
                int index = 0;
                for (WoozRoute woozRoute : routes) {
                    double distance = 0;
                    if (precise)
                        distance = WoozTools.getDistance(woozRoute.lat, woozRoute.lng, raw.lat, raw.lng);
                    else
                        distance = WoozTools.distanceSimplify(woozRoute.lat, woozRoute.lng, raw.lat, raw.lng);
                    if (distance < maxAway) {
                        raw.closestRoutePoints.add(new Mt90Raw.CloseRoutePoint(index, distance));
                    }
                    index ++;
                }
                if (raw.closestRoutePoints.isEmpty())
                    continue;
                if (raw.closestRoutePoints.size() > 1)
                    Collections.sort(raw.closestRoutePoints);
                newList.add(raw);
                //System.out.println(raw.closestRoutePoints);
            }
            //log.infof("过滤前%d 过滤后%d", list.size(), newList.size());
            list = newList;
        }
        sw.tag("剔除所有离线路太远的点");
        {
            List<Mt90Raw> newList = new ArrayList<>();
            int lastRoutePoint = 0;
            Mt90Raw prev = list.get(0);
            int index = 0;
            for (Mt90Raw raw : list) {
                for (Mt90Raw.CloseRoutePoint point : raw.closestRoutePoints) {
                    if (point.pointIndex >= lastRoutePoint) {
                        // 合理性判断, 水平速度不可能超过30km/h吧?
                        WoozRoute last = routes.get(lastRoutePoint);
                        WoozRoute cur = routes.get(point.pointIndex);
                        double len = cur.countDistance - last.countDistance;
                        long time = raw.timestamp - prev.timestamp;
                        int speed = (int) Math.abs(len / (time / 1000 + 1) * 3.6);
                        if (debug)
                            log.infof("选手轨迹点序号%d 线路轨迹点%d 水平速度%dkm/s", index, point.pointIndex, speed);
                        if (time != 0 && speed > 30) {
                            if (debug)
                                log.infof("选手轨迹点序号%d 线路轨迹点%d 速度太夸张了, 不合理, 跳过.", index, point.pointIndex);
                            continue;
                        }
                        else {
                            if (debug)
                                log.infof("选手轨迹点序号%d 线路轨迹点%d 速度合理,选用之", index, point.pointIndex);
                        }
                            
                        raw.closestRouteIndex = point.pointIndex;
                        lastRoutePoint = point.pointIndex;
                        prev = raw;
                        newList.add(raw);
                        break;
                    }
                }
                if (raw.closestRouteIndex < 0) {
                    if (debug)
                        log.infof("选手轨迹点序号%d 没有合适的线路点匹配", index);
                }
                index ++;
            }
            if (debug)
                log.infof("经过赛项线路匹配, 过滤前%d 过滤后%d", list.size(), newList.size());
            list = newList;
        }
        sw.tag("赛项线路匹配");
        if (list.isEmpty())
            return;
        
        
        // =================================预测============================================
        // 来吧, 列3个3元一次方程
        if (list.size() < 10) {
            // 点太少了,不算了
        }
        else {

            // 往前找30分钟
            NutMap[] speeds = new NutMap[list.size()];
            int[] _re = new int[1];
            boolean checkAll = hc.params.is("all");
            for (int i = list.size() - 1; i >= 10; i--) {
                // 暂时禁用方程式, 直接按固定速度算
//                double[] result = countResult(list, routes, i, _re, debug);
//                if (result != null) {
//                    NutMap tmp = new NutMap();
//                    if (checkAll) {
//                        tmp.put("distance", result[0]);
//                        tmp.put("up", result[1]);
//                        tmp.put("down", result[2]);
//                    }
//                    
//                    // 到达CP点的耗时
//                    WoozRoute route = routes.get(list.get(i).closestRouteIndex);
//                    double t = route.cpDistance * result[0] + route.cpUp * result[1] + route.cpDown * result[2];
//                    tmp.put("eta_cp", (int)t);
//                    tmp.put("eta_cp_time", list.get(i).gpsDate.getTime() + (int)t*1000);
//                    // 到达终点的耗时
//                    WoozRoute route_end = routes.get(routes.size() - 1);
//                    double t2 = (route_end.countDistance - route.countDistance) * result[0] + (route_end.countUp - route.countUp) * result[1] + (route_end.countDown - route.countDown) * result[2];
//                    tmp.put("eta_end", (int)t2);   
//                    tmp.put("eta_end_time", list.get(i).gpsDate.getTime() + (int)t2*1000);                 
//                    speeds[i] = tmp;
//                    if (!re.containsKey("eta_cp_time")) {
//                        re.putAll(tmp);
//                        re.put("u_eta_route_index", list.get(i).closestRouteIndex);
//                    }
//                    if (!checkAll) {
//                        break;
//                    }
//                    if (debug)
//                        log.infof("轨迹点%06d 打卡点%-10s 水平每米耗时 %.2f 上升每米耗时 %.2f 下降每米耗时 %.2f 预计到终点的耗时 %s", i, route.cpName, result[0], result[1], result[2], (int)t2);
//                }
            }
            if (checkAll) {
                re.put("speeds", speeds);
            }
            
            // 一个预测结果都没有啊?!!! 按平均速度算, 8km/h, 即 8000/3600.0的米耗时
            if (!re.has("eta_cp_time")) {
                Mt90Raw last = list.get(list.size() - 1);
                double[] result = new double[] {3600.0/8000, 0, 0};
                WoozRoute route = routes.get(last.closestRouteIndex);
                double t = (route.cpDistance + route.cpUp * 10) * result[0];
                re.put("eta_cp", (int)t);
                re.put("eta_cp_time", last.gpsDate.getTime() + (int)t*1000);
                // 到达终点的耗时
                WoozRoute route_end = routes.get(routes.size() - 1);
                double t2 = (route_end.countDistance - route.countDistance + (route_end.countUp - route.countUp)*10) * result[0];
                re.put("eta_end", (int)t2);   
                re.put("eta_end_time", last.gpsDate.getTime() + (int)t2*1000);
            }
            
            if (checkAll && hc.params.has("image")) {

                // 测试性画个图看看
                double[] distance = new double[list.size()]; // 水平速度
                double[] up = new double[list.size()]; // 上升速度
                double[] down = new double[list.size()]; // 下降速度
                double[] eta_cp = new double[list.size()]; // 预计到达CP点的耗时
                double[] eta_end = new double[list.size()]; // 预计到达终点的耗时
                double[] rssi = new double[list.size()]; // GSM信号强度
                double[] satellite = new double[list.size()]; // 可见卫星数
                double[] ele = new double[list.size()]; // 海拔
                
                NutMap preSpeed = null;
                for (int i = 0; i < list.size(); i++) {
                    if (speeds[i] != null) {
                        preSpeed = speeds[i];
                    }
                    if (preSpeed != null) {
                        distance[i] = preSpeed.getDouble("distance");
                        up[i] = preSpeed.getDouble("up");
                        down[i] = preSpeed.getDouble("down"); 
                        eta_cp[i] = preSpeed.getDouble("eta_cp");
                        eta_end[i] = preSpeed.getDouble("eta_end");
                    }
                    rssi[i] = list.get(i).gsmRssi;
                    satellite[i] = list.get(i).satellite;
                    ele[i] = list.get(i).ele;
                }
                Pattern p = Pattern.compile(hc.params.get("image_lines",".+"));
                try (ReportImageBuilder builder = new ReportImageBuilder(list.size(), 1000, Color.WHITE)) {
                    builder.drawCoordinate(Color.BLACK);
                    if (p.matcher("distance").find())
                        builder.drawData("水平速度", Color.RED, distance, 100, -20);
                    if (p.matcher("up").find())
                        builder.drawData("爬升速度", Color.YELLOW, up, 100, -20);
                    if (p.matcher("down").find())
                        builder.drawData("下降速度", Color.GREEN, down, 100, -20);
                    if (p.matcher("eta_cp").find())
                        builder.drawData("预计到达CP点的耗时", Color.ORANGE, eta_cp, 86400, 0);
                    if (p.matcher("eta_end").find())
                        builder.drawData("预计达到终点的耗时", Color.PINK, eta_end, 86400, 0);
                    
                    if (p.matcher("rssi").find())
                        builder.drawData("GSM信号强度", Color.BLACK, rssi, 31, 0);
                    if (p.matcher("satellite").find())
                        builder.drawData("卫星数量", Color.RED, satellite, 20, 0);
                    if (p.matcher("ele").find())
                        builder.drawData("海拔", Color.BLUE, ele, 2000, 0);
                    sys.io.writeImage(sys.io.createIfNoExists(null, Wn.normalizeFullPath(hc.params.get("image"), sys), WnRace.FILE), builder.getImage());
                }
            }
        }
        sw.tag("预测");

        // 更新 u_trk_route_index
        if (hc.params.vals.length > 0 && !hc.params.is("all")) {
            String path = Wn.normalizeFullPath(hc.params.val_check(0), sys);
            WnObj trk_data = sys.io.check(null, path);
            NutMap metas = re;
            metas.put("u_trk_route_index", list.get(list.size() - 1).closestRouteIndex);
            sys.io.appendMeta(trk_data, metas);
        }
        sys.out.writeJson(re, Cmds.gen_json_format(hc.params));


        sw.tag("全部完成");
        sw.stop();
        if (debug)
            log.info(">> " + sw.toString());
    }

    public void printMatrix(double[][] matrix) {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n");
        for (int i = 0; i < matrix.length; i++) {
            sb.append("[");
            for (int j = 0; j < matrix[0].length; j++) {
                sb.append(Long.toString((long)matrix[i][j])).append(' ');
            }
            sb.setCharAt(sb.length() - 1, ']');
            sb.append("\r\n");
        }
        log.info("matrix=\r\n"  + sb.toString());
    }
    
    public void checkMatrixResult(double[][] matrix, double[] result) {
        if (result == null) {
            //log.info("no result!!!");
            return;
        }
        double[] diff = new double[result.length];
        for (int i = 0; i < matrix.length; i++) {
            double t = matrix[i][matrix[0].length - 1];
            for (int j = 0; j < result.length; j++) {
                t -= matrix[i][j] * result[j];
                //log.infof("%s*%s=%s    %s", matrix[i][j], result[j], matrix[i][j] * result[j], t);
            }
            diff[i] = Math.abs(t);
        }
        log.info("验算结果:" + Json.toJson(diff));
    }
    
    public double[] countResult(List<Mt90Raw> list, List<WoozRoute> routes, int endAt, int[] _re, boolean debug) {
        Mt90Raw _25min = list.get(endAt);
        
        List<Integer> tlist = new ArrayList<>();
        tlist.add(endAt);
        for (int i = endAt -1; i > -1; i--) {
            if (_25min.timestamp - list.get(i).timestamp < 30*60*1000) {
                tlist.add(Integer.valueOf(i));
            }
            else {
                break;
            }
        }
        //log.infof("count %d point for 3-1", tlist.size());
        if (tlist.size() > 40) {
            // 抽取0,5,10,15,20,25
            // 0 - 30 , 0 - 20, 10 - 30
            int _t = tlist.size() / 5;
            Mt90Raw _0min = list.get(tlist.get(tlist.size() - 1));
            Mt90Raw _5min = list.get(tlist.get(_t*1));
            Mt90Raw _10min = list.get(tlist.get(_t*2));
            Mt90Raw _15min = list.get(tlist.get(_t*3));
            Mt90Raw _20min = list.get(tlist.get(_t*4)) ;
            //Mt90Raw _25min = list.get(tlist.get(tlist.size() / 3));
            
            // 准备个矩阵
            double[][] matrix = new double[3][4];
            // 10-25的3个参数
            matrix[0][0] = (routes.get(_25min.closestRouteIndex).countDistance - routes.get(_10min.closestRouteIndex).countDistance);
            matrix[0][1] = (routes.get(_25min.closestRouteIndex).countUp - routes.get(_10min.closestRouteIndex).countUp);
            matrix[0][2] = (routes.get(_25min.closestRouteIndex).countDown - routes.get(_10min.closestRouteIndex).countDown);
            matrix[0][3] = (_25min.timestamp - _10min.timestamp) / 1000;
            // 5 - 20的三个参数
            matrix[1][0] = (routes.get(_20min.closestRouteIndex).countDistance - routes.get(_5min.closestRouteIndex).countDistance);
            matrix[1][1] = (routes.get(_20min.closestRouteIndex).countUp - routes.get(_5min.closestRouteIndex).countUp);
            matrix[1][2] = (routes.get(_20min.closestRouteIndex).countDown - routes.get(_5min.closestRouteIndex).countDown);
            matrix[1][3] = (_20min.timestamp - _5min.timestamp) / 1000;
            // 0 - 15的3个参数
            matrix[2][0] = (long) (routes.get(_15min.closestRouteIndex).countDistance - routes.get(_0min.closestRouteIndex).countDistance);
            matrix[2][1] = (long) (routes.get(_15min.closestRouteIndex).countUp - routes.get(_0min.closestRouteIndex).countUp);
            matrix[2][2] = (long) (routes.get(_15min.closestRouteIndex).countDown - routes.get(_0min.closestRouteIndex).countDown);
            matrix[2][3] = (_15min.timestamp - _0min.timestamp) / 1000;
            
            double[][] _matrix = null;
            if (debug) {
                printMatrix(matrix);
                _matrix = new double[matrix.length][matrix[0].length];
                for (int i = 0; i < _matrix.length; i++) {
                    for (int j = 0; j < _matrix[0].length; j++) {
                        _matrix[i][j] = matrix[i][j];
                    }
                }
            }
            
            PolynomialSoluter soluter = new PolynomialSoluter(matrix);
            double[] result = soluter.getResult();
            if (result == null)
                return null;
            if (debug) {
                log.info(Json.toJson(result));
                // 验算一下结果
                checkMatrixResult(_matrix, result);
            }
            if (result[0] == Double.NEGATIVE_INFINITY || result[0] == Double.NEGATIVE_INFINITY) {
                return null;
            }
            if (result[1] == Double.NEGATIVE_INFINITY || result[1] == Double.NEGATIVE_INFINITY) {
                return null;
            }
            if (result[2] == Double.NEGATIVE_INFINITY || result[2] == Double.NEGATIVE_INFINITY) {
                return null;
            }
            if (Math.abs(result[0]) > 10 || Math.abs(result[1]) > 10 || Math.abs(result[2]) > 10) {
                // 速度值不在合理范围之内,拒绝
                return null;
            }
            //_re[0] = _30min_index;
            return result;
        }
        return null;
    }
}
