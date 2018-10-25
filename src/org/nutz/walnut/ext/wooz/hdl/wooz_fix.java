package org.nutz.walnut.ext.wooz.hdl;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.ext.wooz.WoozMap;
import org.nutz.walnut.ext.wooz.WoozPoint;
import org.nutz.walnut.ext.wooz.WoozRoute;
import org.nutz.walnut.ext.wooz.WoozTools;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value="cqn", regex="^(cplist)$")
public class wooz_fix implements JvmHdl {
    
    private static final Log log = Logs.get();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        Reader r;
        if (sys.pipeId > 0) {
            r =  sys.in.getReader();
        }
        else {
            r = sys.io.getReader(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)), 0);
        }
        BufferedReader br = new BufferedReader(r);
        WoozMap wooz = Json.fromJson(WoozMap.class, br);
        // 有没有CP点
        if (wooz == null || wooz.points == null || wooz.points.isEmpty()) {
            sys.err.print("e.cmd.wooz.fix.without_any_cp");
        }
        boolean flag = true;
        WoozPoint startPoint = null;
        WoozPoint endPoint = null;
        List<WoozPoint> cplist = new ArrayList<>(); // 包含起点和终点的CP列表
        log.debug("获取cplist");
        for (WoozPoint point : wooz.points) {
            if ("csp".equals(point.type)) {
                flag = false;
            }
            else if ("start".equals(point.type)) {
                startPoint = point;
            }
            else if ("end".equals(point.type)) {
                endPoint = point;
            }
            else {
                continue;
            }
            cplist.add(point);
        }
        if (flag) {
            sys.err.print("e.cmd.wooz.fix.without_any_cp");
            return;
        }
        if (startPoint == null || endPoint == null) {
            sys.err.print("e.cmd.wooz.fix.without_start_or_end");
            return;
        }
        log.debug("计算每个CP点对应的轨迹点");
        // 计算每个CP点对应的轨迹点
        for (WoozPoint point : wooz.points) {
            if ("start".equals(point.type)) {
                point.routePointIndex = 0;
                point.routePointDistance = 0;
            }
            else if ("end".equals(point.type)) {
                point.routePointIndex = wooz.route.size() - 1; // 直接定位到最后一个点
                point.routePointDistance = 0; // 肯定是0
            }
            if (!"csp".equals(point.type)) {
                continue;
            }
            // 查找与CP点最近的轨迹点
            int[] re = WoozTools.findClosest(wooz.route, point.lat, point.lng, -1);
            point.routePointIndex = re[0];
            point.routePointDistance = re[1];
        }
        log.info("重新排序");
        // 重新排序
        cplist.remove(startPoint);
        cplist.add(0, startPoint);
        cplist.remove(endPoint);
        cplist.add(endPoint);
        for (WoozPoint woozPoint : cplist) {
            log.infof("CP点: %s 轨迹点索引 %d", woozPoint.name, woozPoint.routePointIndex);
        }
        
        log.info("校验cplist里面的轨迹点顺序");
        // 校验cplist里面的轨迹点顺序
        for (int i = 0; i < cplist.size()-1; i++) {
            WoozPoint cur = cplist.get(i);
            WoozPoint next = cplist.get(i+1);
            if (cur.routePointIndex > next.routePointIndex) {
                sys.err.print("cp点顺序不合法!!!");
                return;
            }
        }
        
        log.info("计算每个轨迹点相对于前一个轨迹点的爬升/下降,距离");
        // 计算每个轨迹点相对于前一个轨迹点的爬升/下降,距离
        Iterator<WoozRoute> it = wooz.route.iterator();
        WoozRoute preRoutePoint = it.next();
        while (it.hasNext()) {
            WoozRoute cur = it.next();
            if (cur.ele > 0 && preRoutePoint.ele > 0) { // 有时候海拔是0,需要忽略
                if (preRoutePoint.ele > cur.ele) { // 下降
                    cur.goDown = preRoutePoint.ele - cur.ele;
                }
                else if (preRoutePoint.ele < cur.ele) { // 上升
                    cur.goUp = cur.ele - preRoutePoint.ele;
                }
            }
            cur.goDistance = WoozTools.getDistance(cur.lat, cur.lng, preRoutePoint.lat, preRoutePoint.lng);
            //System.out.println("轨迹点 距离 " + cur.goDistance);
            preRoutePoint = cur;
        }
        
        log.info("计算CP点之间的距离");
        // 计算CP点之间的距离
        WoozPoint prevCp = null;
        for (int i = 0; i < cplist.size(); i++) {
            WoozPoint point = cplist.get(i);
            // 当前CP点所经历的轨迹点索引
            int start = 0;
            int end = point.routePointIndex;
            if (prevCp != null) {
                start = prevCp.routePointIndex;
            }
            
            double goUp = 0;
            double goDown = 0;
            double goDistance = 0;
            for (int j = start+1; j < end; j++) {
                goUp += wooz.route.get(j).goUp;
                goDown += wooz.route.get(j).goDown;
                goDistance += wooz.route.get(j).goDistance;
            }
            point.distancePrev = (int) goDistance;
            point.goUp = (int) goUp;
            point.goDown = (int) goDown;
            prevCp = point;
        }
        
        log.debug("再遍历一遍,算出CP点的更多信息");
        // 再遍历一遍,算出CP点的更多信息
        for (int i = 0; i < cplist.size(); i++) {
            WoozPoint cur = cplist.get(i);
            // 算起点到当前点的距离
            cur.distanceStart = 0;
            cur.distanceEnd = 0;
            for (int j = 1; j <= i; j++) {
                cur.distanceStart += cplist.get(j).distancePrev;
            }
            for (int j = i+1; j < cplist.size(); j++) {
                cur.distanceEnd += cplist.get(j).distancePrev;
            }
            if (i < cplist.size() - 1) {
                WoozPoint next = cplist.get(i+1);
                cur.distanceNext = next.distancePrev;
                cur.nextUp = next.goUp;
                cur.nextDown = next.goDown;
            }
        }
        
        // 仅输出cplist,调试用
        if (hc.params.is("cplist")) {
            sys.out.writeJson(cplist, Cmds.gen_json_format(hc.params));
        }
        // 输出完整的wooz map
        else {
            sys.out.writeJson(wooz, Cmds.gen_json_format(hc.params));
        }
        
    }

}
