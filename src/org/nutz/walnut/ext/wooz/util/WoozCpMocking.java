package org.nutz.walnut.ext.wooz.util;

import java.util.Date;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.Times.TmInfo;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.wooz.WoozMap;
import org.nutz.walnut.ext.wooz.WoozPoint;
import org.nutz.walnut.util.Wn;

public class WoozCpMocking {

    private WnOutputable out;

    private WnIo io;

    private int speed;

    private int playerLimit;

    private boolean cleanAllRecords;

    private WnObj oComp;

    private List<WnObj> oPlyList;

    private String cpdName;

    private WnObj dTrkcp;

    private WnObj dProj;

    private WnObj dSignup;

    public WoozCpMocking(WnOutputable out, WnIo io, String compId) {
        this.io = io;
        // 默认设备名称
        cpdName = "CPD-WZ0-001";
        // 得到赛事对象，仙踪域的赛事，需要转换为主办方赛事
        oComp = io.checkById(compId);
        if (oComp.has("sp_comp_id")) {
            oComp = io.checkById(oComp.getString("sp_comp_id"));
        }
        // 得到所有关键目录
        dProj = __comp_dir("proj");
        dTrkcp = __comp_dir("trkcp");
        dSignup = __comp_dir("signup");
    }

    private WnObj __comp_dir(String name) {
        // 得打卡记录目录
        String phTrkcp = Wn.appendPath("/home", oComp.d1(), "comp/data", oComp.id(), name);
        return io.check(null, phTrkcp);
    }

    public void doMock() {
        // 清除所有的打卡记录
        if (cleanAllRecords) {
            List<WnObj> oCpList = io.getChildren(dTrkcp, null);
            for (WnObj oCp : oCpList) {
                io.delete(oCp);
            }
        }

        // 获取选手列表
        WnQuery q = Wn.Q.pid(dSignup);
        q.setv("s_tp", "player");
        q.setv("u_pj", "");
        if (playerLimit > 0) {
            q.limit(playerLimit);
        }
        oPlyList = io.query(q);

        // 循环处理选手
        int i_ply = 0;
        for (WnObj oPly : oPlyList) {
            String uid = oPly.name();
            String unm = oPly.getString("u_nm");

            // 输出信息
            if (null != out) {
                out.printlnf("%3d/%3d: Gen cp for '%s':", i_ply, oPlyList.size(), unm);
            }

            // 得到选手对应的赛项
            String pj = oPly.getString("u_pj");
            WnObj oPj = io.check(dProj, pj);

            // 得到选手赛项的轨迹信息
            WnObj oPjMap = io.check(oPj, "mars_google.json");
            WoozMap pjMap = io.readJson(oPjMap, WoozMap.class);

            // 得到赛项开赛时间
            long msPjStart = oPj.getLong("d_start");

            // 累计用时（毫秒）
            long du_sum = 0;

            // 循环赛点列表
            int poIndex = 0;
            for (WoozPoint po : pjMap.points) {
                // 本赛段的用时（毫秒）
                long du_phase = 0;
                // 如果是起点，那么直接根据赛项起始时间，建立打卡记录
                if ("start".equals(po.type)) {
                    // 貌似啥也不用干
                }
                // 其他赛点，则模拟计算时间
                else if ("end".equals(po.type) || "cp".equals(po.type)) {
                    // 得到选手在本赛段的速度 在 30% 的范围浮动
                    // 速度单位为 (米/小时)
                    int speedDiff = R.random(0, 30) + 100;
                    int mySpeed = speed * speedDiff / 100;

                    // 计算 po 在上一赛段的用时（毫秒）
                    du_phase = po.distancePrev * 3600000L / mySpeed;
                    du_sum += du_phase;

                }
                // 其他类型赛点，无视
                else {
                    poIndex++;
                    continue;
                }

                // 那么本地打卡时间
                long ms_cpr_tm = msPjStart + du_sum;
                Date d_cpr_tm = Times.D(ms_cpr_tm);
                String s_cpr_tm = Times.format("yyyyMMdd'T'HHmmss", d_cpr_tm);

                // 为每个打卡点创建一个打卡记录
                String nm = pj + "_" + po.name + "_" + uid + "_" + s_cpr_tm;
                NutMap meta = new NutMap();
                meta.put("cp_dev", this.cpdName);
                meta.put("u_pj", pj);
                meta.put("u_id", uid);
                meta.put("u_nm", unm);
                meta.put("u_aa", oPly.getString("u_aa"));
                meta.put("u_sex", oPly.getString("sex"));
                meta.put("u_code", oPly.getString("u_code"));
                meta.put("cp_nm", po.name);
                meta.put("cp_index", poIndex);
                meta.put("cpr_tm", ms_cpr_tm);
                meta.put("cpr_st", "C");
                meta.put("dev_lat", po.lat);
                meta.put("dev_lng", po.lng);
                meta.put("dev_ele", po.ele);
                meta.put("du_total", du_sum);
                meta.put("du_phase", du_phase);
                meta.put("c", oComp.creator());
                meta.put("g", oComp.group());

                // 创建记录
                WnObj oCp = io.create(dTrkcp, nm, WnRace.FILE);
                io.appendMeta(oCp, meta);

                // 输出信息
                if (null != out) {
                    TmInfo ti_sum = Times.Tims(du_sum);
                    TmInfo ti_phase = Times.Tims(du_phase);
                    out.printlnf(" - %d) %s > %s @ %s : %s/%s : %s",
                                 poIndex,
                                 pj,
                                 unm,
                                 po.name,
                                 ti_phase.toString("HH:mm:ss"),
                                 ti_sum.toString("HH:mm:ss"),
                                 s_cpr_tm);
                }

                // 下一个赛点
                poIndex++;
            }

            // 下一个选手
            i_ply++;
        }
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public boolean isCleanAllRecords() {
        return cleanAllRecords;
    }

    public void setCleanAllRecords(boolean cleanAllRecords) {
        this.cleanAllRecords = cleanAllRecords;
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    public String getCpdName() {
        return cpdName;
    }

    public void setCpdName(String cpdName) {
        if (!Strings.isBlank(cpdName))
            this.cpdName = cpdName;
    }

}
