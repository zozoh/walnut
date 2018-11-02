package org.nutz.walnut.ext.mt90.hdl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.json.Json;
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

@JvmHdlParamArgs(value="cqn", regex="^(kml|gpx|gpsFixed|simple|lineOnly|debug|tomap|all)$")
public class mt90_map_match extends mt90_parse {
    
    private static final Log log = Logs.get();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        hc.params.setv("simple", true);
        WoozMap map = Mt90Map.get(sys.io, Wn.normalizeFullPath(hc.params.check("map"), sys));
        boolean debug = hc.params.is("debug");
        List<Mt90Raw> list = parse(sys, hc);
        List<WoozRoute> routes = map.route;
        NutMap re = new NutMap();
        
        // =================================轨迹匹配============================================
        int[] _match = new int[list.size()];
        Arrays.fill(_match, -1);
        // 第一个点总是固定在起点
        _match[0] = 0;
        // 因时间原因跳过了,多少个点
        int skipTimeCount = 0;
        // 因轨迹点原因跳过了多少个点
        int skipDistanceCount = 0;
        // 前一个可用的轨迹点
        int prevOkRouteIndex = 0;
        for (int i = 1; i < _match.length; i++) {
            // 前一个轨迹点
            Mt90Raw prev = list.get(i-1);
            Mt90Raw cur = list.get(i);
            if (cur.gpsDate.getTime() - prev.gpsDate.getTime() > 120*1000) {
                //log.debug("轨迹点的时间间隔有点大, 跳过当前点");
                skipTimeCount ++;
                continue;
            }
            // 前一个点对应的位置
            int prev_route_index = _match[i-1];
            if (prev_route_index == -1) {
                prev_route_index = 0;
            }
            // 找少于50米的点
            boolean flag = false;
//            for (int j = prev_route_index; j < routes.size(); j++) {
//                WoozRoute tmp = routes.get(j);
//                double len = WoozTools.getDistance(tmp.lat, tmp.lng, cur.lat, cur.lng);
//                if (len < 150) { // 少于50米,可以了
//                    prevOkRouteIndex = j;
//                    flag = true;
//                }
//            }
            // 找少于100米的点
            if (!flag) {
                for (int j = prev_route_index; j < routes.size(); j++) {
                    WoozRoute tmp = routes.get(j);
                    double len = WoozTools.getDistance(tmp.lat, tmp.lng, cur.lat, cur.lng);
                    if (len < 100) { // 少于100米,勉强吧
                        prevOkRouteIndex = j;
                        flag = true;
                    }
                }
            }
            if (flag) {
                _match[i] = prevOkRouteIndex;
            }
            else {
                // 啊啊啊没有匹配的轨迹点呀
                skipDistanceCount ++;
            }
        }
        List<WoozRoute> _routes = new ArrayList<>();
        int u_trk_route_index = 0;
        for (int i : _match) {
            if (i > -1) {
                _routes.add(routes.get(i));
                u_trk_route_index = i;
            }
        }
        re.put("skipTimeCount", skipTimeCount);
        re.put("skipDistanceCount", skipDistanceCount);
        re.put("trkCount", list.size());
        
        // 更新 u_trk_route_index
        if (hc.params.vals.length > 0) {
            String path = Wn.normalizeFullPath(hc.params.val_check(0), sys);
            WnObj trk_data = sys.io.check(null, path);
            sys.io.appendMeta(trk_data, new NutMap("u_trk_route_index", u_trk_route_index));
        }
        
        // =================================预测============================================
        // 来吧, 列3个3元一次方程
        if (_routes.size() < 10) {
            // 点太少了,不算了
        }
        else {
            // 往前找30分钟
            NutMap[] speeds = new NutMap[_match.length];
            int[] _re = new int[1];
            int end = hc.params.is("all") ? 10 : _match.length - 1;
            NutMap firstResult = null;
            for (int i = _match.length - 1; i >= end; i--) {
                double[] result = countResult(list, routes, _match, i, _re, debug);
                if (result != null) {
                    int lastest_route_index = _re[0]; 
                    NutMap tmp = new NutMap();
                    tmp.put("distance", result[0]);
                    tmp.put("up", result[1]);
                    tmp.put("down", result[2]);
                    
                    // 到达CP点的耗时
                    WoozRoute route = routes.get(lastest_route_index);
                    double t = route.cpDistance * result[0] + route.cpUp * result[1] + route.cpDown * result[2];
                    re.put("eta_cp", (int)t);
                    // 到达终点的耗时
                    route = routes.get(routes.size() - 1);
                    double t2 = route.cpDistance * result[0] + route.cpUp * result[1] + route.cpDown * result[2];
                    re.put("eta_end", (int)t2);
                    
                    speeds[lastest_route_index] = tmp;
                    if (firstResult == null) {
                        firstResult = tmp;
                        re.put("lastest_speed", tmp);
                    }
                }
            }
            re.put("speeds", speeds);
            
            if (hc.params.has("image")) {

                // 测试性画个图看看
                double[] distance = new double[_match.length]; // 水平速度
                double[] up = new double[_match.length]; // 上升速度
                double[] down = new double[_match.length]; // 下降速度
                double[] eta_cp = new double[_match.length]; // 预计到达CP点的耗时
                double[] eta_end = new double[_match.length]; // 预计到达终点的耗时
                double[] rssi = new double[_match.length]; // GSM信号强度
                double[] satellite = new double[_match.length]; // 可见卫星数
                double[] ele = new double[_match.length]; // 海拔
                
                NutMap preSpeed = null;
                for (int i = 0; i < _match.length; i++) {
                    if (speeds[i] != null) {
                        preSpeed = speeds[i];
                    }
                    if (preSpeed != null) {
                        distance[i] = preSpeed.getDouble("distance");
                        up[i] = preSpeed.getDouble("up") / 10 ; // 画图的时候,下降和上升都10%够了
                        down[i] = preSpeed.getDouble("down") / 10; 
                        eta_cp[i] = preSpeed.getDouble("eta_cp");
                        eta_end[i] = preSpeed.getDouble("eta_end");
                    }
                    rssi[i] = list.get(i).gsmRssi;
                    satellite[i] = list.get(i).satellite;
                    ele[i] = list.get(i).ele;
                }
                Pattern p = Pattern.compile(hc.params.get("image_lines",".+"));
                try (ReportImageBuilder builder = new ReportImageBuilder(_match.length, 1000, Color.WHITE)) {
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
        
        if (hc.params.is("tomap")) {
            re.put("route", _routes);
            re.put("points", new ArrayList<>());
        }
        sys.out.writeJson(re, Cmds.gen_json_format(hc.params));
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
        log.info(sb.toString());
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
        log.info(Json.toJson(diff));
    }
    
    public double[] countResult(List<Mt90Raw> list, List<WoozRoute> routes, int[] _match, int endAt, int[] _re, boolean debug) {
        Mt90Raw _30min = null;
        
        List<Integer> tlist = new ArrayList<>();
        for (int i = endAt; i > -1; i--) {
            int t = _match[i];
            if (t < 0) {
                continue;
            }
            if (_30min == null) {
                _30min = list.get(i);
                tlist.add(Integer.valueOf(i));
            }
            else {
                if (_30min.gpsDate.getTime() - list.get(i).gpsDate.getTime() < 31*60*1000) {
                    tlist.add(Integer.valueOf(i));
                }
                else {
                    break;
                }
            }
        }
        //log.infof("count %d point for 3-1", tlist.size());
        if (tlist.size() > 6) {
            // 抽取0,10,20,30 
            // 0 - 30 , 0 - 20, 10 - 30
            int _0min_index = tlist.get(tlist.size() - 1);
            int _10min_index = tlist.get(tlist.size() / 3);
            int _20min_index = tlist.get(tlist.size() / 3 * 2) ;
            int _30min_index = tlist.get(0);
            
            // 准备个矩阵
            double[][] matrix = new double[3][4];
            // 0 - 30的3个参数
            matrix[0][0] = (routes.get(_30min_index).countDistance - routes.get(_0min_index).countDistance);
            matrix[0][1] = (routes.get(_30min_index).countUp - routes.get(_0min_index).countUp);
            matrix[0][2] = (routes.get(_30min_index).countDown - routes.get(_0min_index).countDown);
            matrix[0][3] = (list.get(_30min_index).gpsDate.getTime() - list.get(_0min_index).gpsDate.getTime()) / 1000;
            // 0 - 20的三个参数
            matrix[1][0] = (routes.get(_20min_index).countDistance - routes.get(_0min_index).countDistance);
            matrix[1][1] = (routes.get(_20min_index).countUp - routes.get(_0min_index).countUp);
            matrix[1][2] = (routes.get(_20min_index).countDown - routes.get(_0min_index).countDown);
            matrix[1][3] = (list.get(_20min_index).gpsDate.getTime() - list.get(_0min_index).gpsDate.getTime()) / 1000;
            // 10 - 30的3个参数
            matrix[2][0] = (long) (routes.get(_30min_index).countDistance - routes.get(_10min_index).countDistance);
            matrix[2][1] = (long) (routes.get(_30min_index).countUp - routes.get(_10min_index).countUp);
            matrix[2][2] = (long) (routes.get(_30min_index).countDown - routes.get(_10min_index).countDown);
            matrix[2][3] = (list.get(_30min_index).gpsDate.getTime() - list.get(_10min_index).gpsDate.getTime()) / 1000;
            
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
            if (debug) {
                log.info(Json.toJson(result));
                // 验算一下结果
                checkMatrixResult(_matrix, result);
            }
            _re[0] = _30min_index;
            return result;
        }
        return null;
    }
}
