package org.nutz.walnut.ext.mt90.hdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.mt90.Mt90Map;
import org.nutz.walnut.ext.wooz.WoozMap;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

/**
 *  根据打卡记录,得出成绩总表
 *
 */
@JvmHdlParamArgs(value="cqn", regex="^(clean|write_result)$")
public class mt90_comp_result implements JvmHdl {
    
    private static final Log log = Logs.get();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备一下必须存在的东西
        // 赛事本身, 主办方域
        WnObj comp = sys.io.check(null, Wn.normalizeFullPath("~/comp/data/"+hc.params.check("comp"), sys));
        // 赛项
        WnObj proj = sys.io.check(null, comp.path() + "/proj/" + hc.params.check("pj"));
        // 打卡记录
        WnObj trkcp = sys.io.check(null, comp.path() + "/trkcp");
        // 跟踪记录
        WnObj trkplayer = sys.io.check(comp, "trkplayer");
        // 地图数据
        WoozMap map = Mt90Map.get(sys.io, proj.path() + "/mars_google.json");
        
        // 查出所有打卡记录, 按时间排序
        WnQuery query = new WnQuery();
        query.setv("pid", trkcp.id());
        query.sortBy("cpr_tm", 1);
        List<WnObj> cprList = sys.io.query(query);
        
        // 查出所有选手
        query = new WnQuery();
        query.setv("pid", trkplayer.id());
        List<WnObj> _players = sys.io.query(query);
        Map<String, WnObj> players = new HashMap<>();
        // 把选手分门别类
        Map<String, PlayerResult> re = new HashMap<>();
        Iterator<WnObj> it = _players.iterator();
        while (it.hasNext()) { // 移除没有选手编号的选手
            WnObj player = it.next();
            if (Strings.isBlank(player.getString("u_code")) && player.getString("u_id") == null) {
                log.info("无效打卡记录: " + player.path());
                it.remove();
                continue;
            }
            players.put(player.getString("u_id"), player);
            PlayerResult pr = new PlayerResult();
            pr.uid = player.getString("u_id");
            pr.player = player;
            re.put(pr.uid, pr);
        }
        // 打卡记录总表
        Map<String, List<String>> globalResult = new HashMap<>();
        for (WnObj cpr : cprList) {
            String uid = cpr.getString("u_id");
            if (uid == null) {
                // 竟然没有对应的选手?
                continue;
            }
            PlayerResult pr = re.get(uid);
            if (pr == null) {
                // 竟然没有对应的选手?
                continue;
            }
            int cpIndex = cpr.getInt("cp_index");
            if (cpIndex >= map.points.size()) {
                // 竟然没有对应的打卡点!!!
                log.info("无效打卡记录, 因为打卡点序号不对 cpIndex="+cpIndex);
                continue;
            }
            String key = cpIndex + "";
            PlayerCpResult pcpr = pr.cps.get(key);
            if (pcpr != null && !cpr.is("u_quit", false)) {
                continue; // 已经打过卡了,且不是退赛记录
            }

            // 全局排名
            List<String> cpPlayerList = globalResult.get(key);
            if (cpPlayerList == null) {
                cpPlayerList = new ArrayList<>();
                globalResult.put(key, cpPlayerList);
            }
            
            // 还得分性别
            String sex_key = key + "_" + pr.player.getString("u_sex", "M");
            List<String> cpPlayerListSex = globalResult.get(sex_key);
            if (cpPlayerListSex == null) {
                cpPlayerListSex = new ArrayList<>();
                globalResult.put(sex_key, cpPlayerListSex);
            }
            
            // 记录个人打卡
            pcpr = new PlayerCpResult();
            pcpr.tm = cpr.getLong("cpr_tm");
            pcpr.rank = cpPlayerList.size() + 1; // 全局排名,从1开始
            pcpr.rank_sex = cpPlayerListSex.size() + 1;
            pr.cps.put(key, pcpr);
            
            // 是否退赛
            if (pr.quit || (cpr.getString("cpr_st") != null && !"C".equals(cpr.getString("cpr_st")))) {
                pr.quit = true;
            }
            else {
                // 计入全局排名
                cpPlayerList.add(uid);
                cpPlayerListSex.add(uid);
            }
            
            // 记录起点和终点
            if (cpIndex == 0) {
                pr.start = pcpr;
            }
            else if (cpIndex == map.points.size() - 1) {
                pr.end = pcpr;
            }
        }
        // 是否写出到result目录呢?
        if (hc.params.is("write_result")) {
            if (comp.is("result_locked", false)) {
                sys.err.print("cmd.mt90.comp.result.compLocked");
                return;
            }
            WnObj result_dir = sys.io.fetch(comp, "result");
            if (result_dir != null && hc.params.is("clean")) {
                sys.io.delete(result_dir, true);
                result_dir = null;
            }
            if (result_dir == null) {
                result_dir = sys.io.createIfNoExists(comp, "result", WnRace.DIR);
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
                
                metas.put("u_pj", player.getString(proj.name()));
                // 看看有无终点打卡记录
                PlayerResult pr = re.get(uid);
                if (pr == null) {
                    metas.put("u_norank", true); // 没有任何成绩
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
                }

                WnObj prz = sys.io.createIfNoExists(result_dir, proj.name() + "_" + uid, WnRace.FILE);
                sys.io.appendMeta(prz, metas);
            }
        }
        
        
        NutMap ret = new NutMap();
        ret.put("allcp", globalResult);
        ret.put("players", re);
        sys.out.writeJson(ret, Cmds.gen_json_format(hc.params));
    }

    public static class PlayerResult {
        public Map<String, PlayerCpResult> cps = new HashMap<>();
        public String uid;
        public transient WnObj player;
        public PlayerCpResult start;
        public PlayerCpResult end;
        public boolean quit;
    }
    
    public static class PlayerCpResult {
        public long tm; /// 打卡时间
        public int rank; // 全局排名
        public int rank_sex; // 同性别排名
    }
}
