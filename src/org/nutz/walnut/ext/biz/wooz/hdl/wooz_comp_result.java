package org.nutz.walnut.ext.biz.wooz.hdl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.biz.wooz.WoozMap;
import org.nutz.walnut.ext.biz.wooz.WoozPoint;
import org.nutz.walnut.ext.iot.mt90.Mt90Map;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlog;

/**
 *  根据打卡记录,得出成绩总表
 *
 */
@JvmHdlParamArgs(value="cqn", regex="^(clean|write|json)$")
public class wooz_comp_result implements JvmHdl {
    
    private static final Log log = Wlog.getCMD();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String compId = hc.params.val_check(0);
        WnObj comp = sys.io.checkById(compId);
        if (comp.has("sp_comp_id")) {
            comp = sys.io.checkById(comp.getString("sp_comp_id"));
            compId = comp.id();
        }
        String compPath = "/home/" + comp.creator() + "/comp/data/" + compId;
        WnObj compDir = sys.io.check(null, compPath);
        WnObj projDir = sys.io.check(compDir, "proj");
        List<WnObj> projs = sys.io.getChildren(projDir, null);
        NutMap ret = new NutMap();
        for (WnObj proj : projs) {
            ret.put(proj.name(), comp_result(comp, compDir, proj.name(), sys, hc));
        }
        if (hc.params.is("json"))
            sys.out.writeJson(ret, hc.jfmt);
    }
    
    public NutMap comp_result(WnObj comp, WnObj compDir, String projName, WnSystem sys, JvmHdlContext hc) {

        // 准备一下必须存在的东西
        // 赛事本身, 主办方域
        // 赛项
        WnObj proj = sys.io.check(compDir, "proj/" + projName);
        // 打卡记录
        WnObj trkcp = sys.io.check(compDir, "trkcp");
        // 跟踪记录
        WnObj trkplayer = sys.io.check(compDir, "trkplayer");
        // 地图数据
        WoozMap map = Mt90Map.get(sys.io, proj.path() + "/mars_google.json");
        
        // 把打卡点做成的map和list
        Map<String, WoozPoint> cpMap = new HashMap<>();
        List<String> cpNames = new ArrayList<>();
        WoozPoint startCP = null;
        for (WoozPoint point : map.points) {
            if ("start".equals(point.type) || "end".equals(point.type) || "cp".equals(point.type)) {
                cpMap.put(point.name, point);
                if ("start".equals(point.type))
                    startCP = point;
                cpNames.add(point.name);
            }
        }
        
        // 比赛开始时间
        long projStart = proj.getLong("d_start");
        
        // 查出所有打卡记录, 按时间排序
        WnQuery query = new WnQuery();
        query.setv("pid", trkcp.id());
        query.sortBy("cpr_tm", 1);
        query.setv("u_pj", proj.name());
        List<WnObj> cprList = sys.io.query(query);
        
        // 查出所有选手
        query = new WnQuery();
        query.setv("pid", trkplayer.id());
        List<WnObj> _players = sys.io.query(query);
        Map<String, WnObj> players = new LinkedHashMap<>();
        // 把选手分门别类
        Map<String, PlayerResult> player_results = new LinkedHashMap<>();
        Iterator<WnObj> it = _players.iterator();
        while (it.hasNext()) { // 移除没有选手编号的选手
            WnObj player = it.next();
            if (Strings.isBlank(player.getString("u_code")) && player.getString("u_id") == null) {
                log.info("无效打卡记录: " + player.path());
                it.remove();
                continue;
            }
            // TODO 根据关门时间进行过滤
            players.put(player.getString("u_id"), player);
            PlayerResult pr = new PlayerResult();
            pr.uid = player.getString("u_id");
            pr.player = player;
            player_results.put(pr.uid, pr);
        }
        // 打卡记录总表
        Map<String, List<String>> globalResult = new LinkedHashMap<>();
        // 第一轮,把每个选手的打卡记录都弄好
        for (WnObj cpr : cprList) {
            String uid = cpr.getString("u_id");
            if (uid == null) {
                // 竟然没有对应的选手?
                if (log.isDebugEnabled())
                    log.debug("cp record without u_id?!!!" + Json.toJson(cpr, JsonFormat.compact()));
                continue;
            }
            if (cpr.getLong("cpr_tm") < projStart) {
                if (log.isDebugEnabled())
                    log.debug("cp record before proj start?!!!" + Json.toJson(cpr, JsonFormat.compact()));
                continue;
            }
            PlayerResult pr = player_results.get(uid);
            if (pr == null) {
                // 竟然没有对应的选手?
                if (log.isDebugEnabled())
                    log.debug("cpr exist but no such player: " + Json.toJson(cpr, JsonFormat.compact()));
                continue;
            }
            WoozPoint cpPoint = null;
            if (cpr.containsKey("cp_nm")) {
                cpPoint = cpMap.get(cpr.getString("cp_nm"));
            }
            if (cpPoint == null) {
                int cpIndex = cpr.getInt("cp_index", 0);
                if (cpIndex >= map.points.size()) {
                    // 竟然没有对应的打卡点!!!
                    log.info("无效打卡记录, 因为打卡点序号不对 cpIndex="+cpIndex);
                    continue;
                }
                cpPoint = map.points.get(cpIndex);
            }
            String key = cpPoint.name;
            PlayerCpResult pcr = pr.cps.get(key);
            if (pcr != null && !cpr.is("u_quit", false)) {
                continue; // 已经打过卡了,且不是退赛记录
            }
            
            // 记录个人打卡
            pcr = new PlayerCpResult();
            pcr.tm = cpr.getLong("cpr_tm");
            pcr.cpRecordStr = Times.format("HH:mm:ss", new Date(pcr.tm));
            pr.cps.put(key, pcr);
            
            // 是否退赛
            if (pr.quit || (cpr.getString("cpr_st") != null && !"C".equals(cpr.getString("cpr_st")))) {
                pr.quit = true;
            }
            
            // 记录起点和终点
            if ("start".equals(cpPoint.type))
                pr.start = pcr;
            else if ("end".equals(cpPoint.type))
                pr.end = pcr;
        }
        // 第二轮, 计算每个选手的耗时
        Map<String, List<PlayerResult>> cps = new LinkedHashMap<>();
        for (PlayerResult pr : player_results.values()) {
            if (pr.start == null) {
                PlayerCpResult pcr = new PlayerCpResult();
                pcr.tm = projStart;
                pr.cps.put(startCP.name, pcr);
                pr.start = pcr;
                pcr.cpRecordStr = "-";
            }
        }
        for (PlayerResult pr : player_results.values()) {
            if (pr.quit)
                continue; // 选手已经退赛,就不用算了
            for (Map.Entry<String, PlayerCpResult> en : pr.cps.entrySet()) {
                String key = en.getKey();
                PlayerCpResult pcr = en.getValue();
                pcr.tused = pcr.tm - pr.start.tm;
                //System.out.println("耗时(毫秒) : " + pcr.tused);
                // 总排名
                List<PlayerResult> prList = cps.get(key);
                if (prList == null) {
                    prList = new ArrayList<>();
                    cps.put(key, prList);
                }
                prList.add(pr);
            }
        }
        // 第三轮, 根据打卡点进行排名
        for (Map.Entry<String, List<PlayerResult>> en : cps.entrySet()) {
            String cpKey = en.getKey();
            List<PlayerResult> prList = en.getValue();
            Collections.sort(prList, new Comparator<PlayerResult>() {
                public int compare(PlayerResult prev, PlayerResult next) {
                    PlayerCpResult pcr_prev = prev.cps.get(cpKey);
                    PlayerCpResult pcr_next = next.cps.get(cpKey);
                    return Long.compare(pcr_prev.tused, pcr_next.tused);
                }
            });
            int rank = 1;
            int rank_m = 1;
            int rank_f = 1;
            Iterator<PlayerResult> it2 = prList.iterator();
            while (it2.hasNext()) {
                PlayerResult pr = it2.next();
                // 总排名
                pr.cps.get(cpKey).rank = rank;
                rank++;
                List<String> resultList = globalResult.get(cpKey);
                if (resultList == null) {
                    resultList = new ArrayList<>();
                    globalResult.put(cpKey, resultList);
                }
                resultList.add(pr.uid);
                
                // 性别排名
                if (pr.player.getString("u_sex", "m").equals("m")) {
                    // 男性
                    pr.cps.get(cpKey).rank_sex = rank_m;
                    rank_m++;
                    resultList = globalResult.get(cpKey + "_m");
                    if (resultList == null) {
                        resultList = new ArrayList<>();
                        globalResult.put(cpKey + "_m", resultList);
                    }
                    resultList.add(pr.uid);
                }
                else {
                    // 女性
                    pr.cps.get(cpKey).rank_sex = rank_f;
                    rank_f++;
                    resultList = globalResult.get(cpKey + "_f");
                    if (resultList == null) {
                        resultList = new ArrayList<>();
                        globalResult.put(cpKey + "_f", resultList);
                    }
                    resultList.add(pr.uid);
                }
            }
        }
        // 第四轮, 选手是否有完成了全部打卡
        Set<String> cpIndexs = cpMap.keySet();
        for (PlayerResult pr : player_results.values()) {
            if (pr.quit) {
                pr.stat = "QUIT"; // 退赛
                continue;
            }
            if (pr.end == null) {
                pr.stat = "NOT_DONE"; // 还没到终点
                continue;
            }
            if (pr.cps.keySet().containsAll(cpIndexs)) {
                pr.stat = "DONE"; // 到终点了,而且全部打卡点都有
            }
            else {
                log.infof("expect %s but %s", Json.toJson(pr.cps.keySet()), Json.toJson(cpIndexs));
                pr.stat = "MISS_CP"; // 到终点了,但还缺少打卡点数据
            }
        }
        
        // 计算选手的区间配速
        for (PlayerResult pr : player_results.values()) {
            if (pr.cps == null)
                continue;
            if (pr.cps.size() < 2)
                continue;
            int pspeed_min = 3600 - 1;
            int pspeed_max = 0;
            for (Map.Entry<String, PlayerCpResult> en : pr.cps.entrySet()) {
                // 每个打卡点的数据,应该对应一个打卡点
                String cpName = en.getKey();
                WoozPoint point = cpMap.get(cpName);
                WoozPoint prevPoint = null;
                if (point == null)
                    continue;
                // 找出当前打卡点的成绩,及上一个打卡点的成绩
                PlayerCpResult prevPcr = null;
                PlayerCpResult pcr = en.getValue();
                if ("start".equals(point.type)) {
                    prevPcr = pcr; // 如果是起点,指向自身就可以了
                    prevPoint = point;
                }
                else {
                    for (WoozPoint point2 : map.points) {
                        if ("start".equals(point2.type) || "end".equals(point2.type) || "cp".equals(point2.type)) {
                            if (point2.name.equals(point.name)) {
                                break;
                            }
                            if (pr.cps.containsKey(point2.name)) {
                                prevPcr = pr.cps.get(point2.name);
                                prevPoint = point2;
                            }
                        }
                    }
                }
                // 当前打卡点与上一个打卡点的距离,在打卡点数据里面有
                pcr.du = point.distancePrev;
                // 量化为字符串 xxx.xx  KM
                pcr.duKM = String.format("%.2f", (pcr.du) / 1000.0);
                // 把打卡点的名字也附上
                if ("start".equals(point.type)) {
                    pcr.cpDesc = "起点";
                }
                else if ("end".equals(point.type)) {
                    pcr.cpDesc = "终点";
                }
                else {
                    pcr.cpDesc = point.name.toUpperCase();
                }

                
                pcr.timeUsedStr = toTimeUsedStr(pcr.tused);
                //System.out.println("耗时(毫秒) : " + pcr.tused + " ------> " + pcr.timeUsedStr);
                if (pcr.du == 0) {
                    // 没有距离,那就是没有速度了
                    pcr.pspeedstr = "-";
                }
                else {
                    // 耗时tusd的单位是毫秒, 距离的单位是米
                    // 所以, 需要各自除1000, 然后算出配速, TODO 加上海拔信息
                    pcr.pspeed = (int)(((pcr.tused - prevPcr.tused)/1000) / ((pcr.du) / 1000.0));
                    pcr.pspeedstr = toPSpeedStr(pcr.pspeed);
                    
                    if (pcr.pspeed > pspeed_max)
                        pspeed_max = pcr.pspeed;
                    if (pcr.pspeed < pspeed_min)
                        pspeed_min = pcr.pspeed;
                }
                
                // 计算总的配速
                if ("end".equals(point.type)) {
                    pr.u_pspeed_avg = (int)(((pcr.tused)/1000) / (point.distanceStart / 1000.0));
                    pr.u_pspeed_avg_str = toPSpeedStr(pr.u_pspeed_avg);
                    
                    pr.timeUsedStr = toTimeUsedStr(pcr.tused);
                    pr.timeUsedFromPjStr = toTimeUsedStr(projStart - pcr.tm);
                }
                pcr.prevCpDesc = prevPoint.desc;
                pcr.prevCpName = prevPoint.name;
            }
            pr.u_pspeed_max = pspeed_max;
            pr.u_pspeed_max_str = toPSpeedStr(pr.u_pspeed_max);
            pr.u_pspeed_min = pspeed_min;
            pr.u_pspeed_min_str = toPSpeedStr(pr.u_pspeed_min);
        }
        
        // 是否写出到result目录呢?
        if (hc.params.is("write")) {
            if (comp.is("result_locked", false)) {
                sys.err.print("cmd.mt90.comp.result.compLocked");
                return new NutMap();
            }
            WnObj result_dir = sys.io.fetch(compDir, "result");
            WnObj signup_dir = sys.io.check(compDir, "signup");
            if (result_dir != null && hc.params.is("clean")) {
                sys.io.delete(result_dir, true);
                result_dir = null;
            }
            if (result_dir == null) {
                result_dir = sys.io.createIfNoExists(compDir, "result", WnRace.DIR);
            }
            
            // 逐个选手记录下来
            for (Map.Entry<String, WnObj> en : players.entrySet()) {
                WnObj player = en.getValue();
                String uid = en.getKey();
                NutMap metas = new NutMap();
                metas.put("u_id", uid);
                metas.put("u_nm", player.getString("u_nm"));
                metas.put("u_aa", player.getString("u_aa"));
                metas.put("u_sex", player.getString("u_sex"));
                metas.put("u_code", player.getString("u_code"));
                
                metas.put("u_pj", proj.name());
                // 看看有无终点打卡记录
                PlayerResult pr = player_results.get(uid);
                if (pr == null) {
                    metas.put("u_norank", true); // 没有任何成绩
                    metas.put("u_stat", "NOT_START");
                }
                else {
                    if (pr.start != null) {
                        metas.put("u_start_tm", pr.start.tm);
                    }
                    if (pr.end == null) { // 还没到达终点?
                        metas.put("rank", -1);
                        metas.put("rank_sex", -1);
                    }
                    else {
                        metas.put("u_end_tm", pr.end.tm);
                        metas.put("rank", pr.end.rank);
                        metas.put("rank_sex", pr.end.rank_sex);
                        if (pr.start != null) {
                            metas.put("du_total", pr.end.tm - pr.start.tm);
                        }
                    }
                    metas.put("rank_cps", pr.cps);
                    metas.put("rank_cps_names", cpNames);
                    metas.put("u_stat", pr.stat);
                    // 配速信息
                    metas.put("u_pspeed_min_str", pr.u_pspeed_min_str);
                    metas.put("u_pspeed_max_str", pr.u_pspeed_max_str);
                    metas.put("u_pspeed_avg_str", pr.u_pspeed_avg_str);
                    // 总成绩信息
                    metas.put("u_tused_gen_str", pr.timeUsedStr); // 暂时用一样的成绩
                    metas.put("u_tused_real_str", pr.timeUsedStr);
                }
                
                metas.put("u_poc_done", false);
                metas.put("u_report_done", false);

                WnObj prz = sys.io.createIfNoExists(result_dir, proj.name() + "_" + uid, WnRace.DIR);
                sys.io.appendMeta(prz, metas);
                if (!prz.creator().equals(compDir.creator())) {
                    sys.io.appendMeta(prz, new NutMap("c", compDir.creator()).setv("g", compDir.creator()));
                }
                WnObj sig = sys.io.fetch(signup_dir, uid);
                if (sig != null) {
                    sys.io.appendMeta(sig, new NutMap("result_id", prz.id()));
                }
            }
        }
        NutMap ret = new NutMap();
        ret.put("allcp", globalResult);
        ret.put("players", player_results);
        return ret;
    }

    public static class PlayerResult {
        public Map<String, PlayerCpResult> cps = new HashMap<>();
        public String uid;
        public transient WnObj player;
        public PlayerCpResult start;
        public PlayerCpResult end;
        public boolean quit;
        public String stat; // "QUIT" 退赛, "MISS_CP" 缺了部分打卡点, "NOT_DONE", 未到达终点, "DONE" 完赛, "NONE_CP" 没有任何打卡点
        
        // 配速信息
        public int u_pspeed_min;
        public int u_pspeed_avg;
        public int u_pspeed_max;
        

        public String u_pspeed_min_str;
        public String u_pspeed_avg_str;
        public String u_pspeed_max_str;
        
        // 总耗时, 用终点打卡成绩算
        public String timeUsedStr;
        public String timeUsedFromPjStr;
    }
    
    public static class PlayerCpResult {
        public long tm; /// 打卡时间
        public long tused; // 从起点到当前点的耗时, 单位是毫秒
        public int rank; // 全局排名
        public int rank_sex; // 同性别排名
        public int du; // 距离,单位是米
        public String duKM; // 距离,单位km, 加小数点后2位
        public String pspeedstr; // xx'XX"
        public int pspeed;
        public String cpDesc; // CP点的中文名
        public String prevCpDesc; // 前一个CP点的中文名
        public String prevCpName; // 前一个CP点的名称
        public String timeUsedStr; // 耗时, 字符串形式
        public String cpRecordStr;   // 打卡时间, 字符串形式
    }
    
    public static String toPSpeedStr(int pspeed) {
        return String.format("%d'%02d\"", pspeed/60, pspeed%60);
    }
    
    public static String toTimeUsedStr(long time_used_ms) {
        long t2 = time_used_ms / 1000;
        long sec = t2 % 60;
        long min = t2 % 3600 / 60;
        long hour = t2 / 3600;
        return String.format("%02d:%02d:%02d", hour, min, sec);
    }
}
