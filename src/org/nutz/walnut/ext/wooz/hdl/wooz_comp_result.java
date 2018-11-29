package org.nutz.walnut.ext.wooz.hdl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.mt90.Mt90Map;
import org.nutz.walnut.ext.wooz.WoozMap;
import org.nutz.walnut.ext.wooz.WoozPoint;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

/**
 *  根据打卡记录,得出成绩总表
 *
 */
@JvmHdlParamArgs(value="cqn", regex="^(clean|write|json)$")
public class wooz_comp_result implements JvmHdl {
    
    private static final Log log = Logs.get();

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
            sys.out.writeJson(ret, Cmds.gen_json_format(hc.params));
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
        
        // 把打卡点做成的map
        Map<String, WoozPoint> cpMap = new HashMap<>();
        WoozPoint startCP = null;
        for (WoozPoint point : map.points) {
            if ("start".equals(point.type) || "end".equals(point.type) || "cp".equals(point.type)) {
                cpMap.put(point.name, point);
            }
            if ("start".equals(point.type))
                startCP = point;
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
            }
            if (pr.quit)
                continue; // 选手已经退赛,就不用算了
            for (Map.Entry<String, PlayerCpResult> en : pr.cps.entrySet()) {
                String key = en.getKey();
                PlayerCpResult pcr = en.getValue();
                pcr.tused = pcr.tm - pr.start.tm;
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
        
        // 是否写出到result目录呢?
        if (hc.params.is("write_result")) {
            if (comp.is("result_locked", false)) {
                sys.err.print("cmd.mt90.comp.result.compLocked");
                return new NutMap();
            }
            WnObj result_dir = sys.io.fetch(compDir, "result");
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
                    if (pr.end == null) { // 还没到达终点?
                        metas.put("rank", -1);
                        metas.put("rank_sex", -1);
                    }
                    else {
                        metas.put("rank", pr.end.rank);
                        metas.put("rank_sex", pr.end.rank_sex);
                        if (pr.start != null) {
                            metas.put("du_total", pr.end.tm - pr.start.tm);
                        }
                    }
                    metas.put("rank_cps", pr.cps);
                    metas.put("u_stat", pr.stat);
                }

                WnObj prz = sys.io.createIfNoExists(result_dir, proj.name() + "_" + uid, WnRace.FILE);
                sys.io.appendMeta(prz, metas);
                if (!prz.creator().equals(compDir.creator())) {
                    sys.io.appendMeta(prz, new NutMap("c", compDir.creator()).setv("g", compDir.creator()));
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
    }
    
    public static class PlayerCpResult {
        public long tm; /// 打卡时间
        public long tused; // 从起点到当前点的耗时, 单位是毫秒
        public int rank; // 全局排名
        public int rank_sex; // 同性别排名
    }
}
