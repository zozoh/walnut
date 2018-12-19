package org.nutz.walnut.ext.wooz.hdl;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.wooz.WoozMap;
import org.nutz.walnut.ext.wooz.WoozPoint;
import org.nutz.walnut.ext.wooz.WoozRoute;
import org.nutz.walnut.ext.wooz.WoozTools;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value = "cqn", regex = "^(cplist|nocsp)$")
public class wooz_fix implements JvmHdl {

    private static final Log log = Logs.get();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 如果 -nocsp 表示不是必须有赛点，默认必须有赛点，也必须有起点和终点
        boolean must_has_csp = !hc.params.has("nocsp");
        // 得到输入源
        String ph = hc.params.val(0);
        Reader r;
        if (Strings.isBlank(ph)) {
            r = sys.in.getReader();
        } else {
            WnObj oF = Wn.checkObj(sys, ph);
            r = sys.io.getReader(oF, 0);
        }
        BufferedReader br = new BufferedReader(r);

        // 解析一哈
        WoozMap wooz = Json.fromJson(WoozMap.class, br);

        // 确保有CP点
        if (must_has_csp) {
            if (wooz == null || wooz.points == null || wooz.points.isEmpty()) {
                // sys.err.print("e.cmd.wooz.fix.without_any_cp");
                throw Er.create("e.cmd.wooz.fix.without_any_cp");
            }
        }
        boolean has_no_any_csp = true;
        WoozPoint startPoint = null;
        WoozPoint endPoint = null;
        List<WoozPoint> cplist = new ArrayList<>(); // 包含起点和终点的CP列表
        if (null != wooz.points) {
            if (log.isDebugEnabled())
                log.debug("获取cplist");
            for (WoozPoint point : wooz.points) {
                if ("cp".equals(point.type) || "sp".equals(point.type)) {
                    has_no_any_csp = false;
                    cplist.add(point);
                } else if ("start".equals(point.type)) {
                    startPoint = point;
                    cplist.add(point);
                } else if ("end".equals(point.type)) {
                    endPoint = point;
                    cplist.add(point);
                }
            }
            if (must_has_csp) {
                // 必须有赛点，或者有 -nocsp
                if (has_no_any_csp) {
                    throw Er.create("e.cmd.wooz.fix.without_any_cp");
                }
                // 必须有起点和终点
                if (startPoint == null || endPoint == null) {
                    throw Er.create("e.cmd.wooz.fix.without_start_or_end");
                }
            }
            if (log.isDebugEnabled())
                log.debug("计算每个CP点对应的轨迹点");
            // 计算每个CP点对应的轨迹点
            Iterator<WoozPoint> it = cplist.iterator();
            while (it.hasNext()) {
                WoozPoint point = it.next();
                if ("start".equals(point.type)) {
                    point.routePointIndex = 0;
                    point.routePointDistance = 0;
                } else if ("end".equals(point.type)) {
                    point.routePointIndex = wooz.route.size() - 1; // 直接定位到最后一个点
                    point.routePointDistance = 0; // 肯定是0
                }
                else {
                    // 查找与CP点最近的轨迹点
                    double[] re = WoozTools.findClosest(wooz.route, point.lat, point.lng, -1);
                    point.routePointIndex = (int) re[0];
                    point.routePointDistance = (int) re[1];
                    System.out.println("" + re[0] + " " + re[1] + " " + point.name + " " + point.lat + " " + point.lng);
                    if ("sp".equals(point.type)) {
                        it.remove();
                    }
                }
            }
            if (log.isInfoEnabled())
                log.info("重新排序");
            // 重新排序
            if (startPoint != null) {
                cplist.remove(startPoint);
                cplist.add(0, startPoint);
            }
            else {
                //log.info("没有起点");
            }
            if (endPoint != null) {
                cplist.remove(endPoint);
                cplist.add(endPoint);
            }
            else {
                //log.info("没有终点");
            }
            if (log.isInfoEnabled()) {
                for (WoozPoint woozPoint : cplist) {
                    log.infof(">>> " + woozPoint);
                    log.infof("CP点: %s 轨迹点索引 %d", woozPoint.name, woozPoint.routePointIndex);
                }

                log.info("校验cplist里面的轨迹点顺序");
            }
        }
        // 校验cplist里面的轨迹点顺序
        for (int i = 0; i < cplist.size() - 1; i++) {
            WoozPoint cur = cplist.get(i);
            WoozPoint next = cplist.get(i + 1);
            if (cur.routePointIndex > next.routePointIndex) {
                throw Er.create("cp点顺序不合法!!! ");
            }
        }
        if (log.isInfoEnabled())
            log.info("计算每个轨迹点相对于前一个轨迹点的爬升/下降,距离");
        // 计算每个轨迹点相对于前一个轨迹点的爬升/下降,距离
        Iterator<WoozRoute> it = wooz.route.iterator();
        WoozRoute preRoutePoint = it.next();
        while (it.hasNext()) {
            WoozRoute cur = it.next();
            if (cur.ele > 0 && preRoutePoint.ele > 0) { // 有时候海拔是0,需要忽略
                if (preRoutePoint.ele > cur.ele) { // 下降
                    cur.goDown = preRoutePoint.ele - cur.ele;
                } else if (preRoutePoint.ele < cur.ele) { // 上升
                    cur.goUp = cur.ele - preRoutePoint.ele;
                }
            } else {
                cur.goDown = 0;
                cur.goUp = 0;
            }
            cur.goDistance = WoozTools.getDistance(cur.lat,
                                                   cur.lng,
                                                   preRoutePoint.lat,
                                                   preRoutePoint.lng);

            cur.countDistance = cur.goDistance + preRoutePoint.countDistance;
            cur.countUp = cur.goUp + preRoutePoint.countUp;
            cur.countDown = cur.goDown + preRoutePoint.countDown;
            // System.out.println("轨迹点 距离 " + cur.goDistance);
            preRoutePoint = cur;
        }
        if (log.isInfoEnabled())
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

            WoozRoute _start = wooz.route.get(start);
            WoozRoute _end = wooz.route.get(end);
            for (int j = start; j < end; j++) {
                WoozRoute route = wooz.route.get(j);
                route.cpDistance = _end.countDistance - route.countDistance;
                route.cpUp = _end.countUp - route.countUp;
                route.cpDown = _end.countDown - route.countDown;
                route.cpName = point.name;
            }
            point.distancePrev = (int) (_end.countDistance - _start.countDistance);
            point.goUp = (int) (_end.countUp - _start.countUp);
            point.goDown = (int) (_end.countDown - _start.countDown);
            prevCp = point;
        }
        if (log.isDebugEnabled())
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
            for (int j = i + 1; j < cplist.size(); j++) {
                cur.distanceEnd += cplist.get(j).distancePrev;
            }
            if (i < cplist.size() - 1) {
                WoozPoint next = cplist.get(i + 1);
                cur.distanceNext = next.distancePrev;
                cur.nextUp = next.goUp;
                cur.nextDown = next.goDown;
            }
        }

        // 仅输出cplist,调试用
        if (hc.params.is("cplist")) {
            // TO wendal: 这里不用生成，直接用 hc.jfmt 就是了，它是父类已经调用好的
            // sys.out.writeJson(cplist, Cmds.gen_json_format(hc.params));
            sys.out.writeJson(cplist, hc.jfmt);
        }
        // 输出完整的wooz map
        else {
            // sys.out.writeJson(wooz, Cmds.gen_json_format(hc.params));
            sys.out.writeJson(wooz, hc.jfmt);
        }

    }

}
