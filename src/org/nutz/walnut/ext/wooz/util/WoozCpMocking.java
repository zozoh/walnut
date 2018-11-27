package org.nutz.walnut.ext.wooz.util;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
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

    private WnObj dResult;

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
        dResult = __comp_dir("result");
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
        for (WnObj oPly : oPlyList) {
            // 得到选手对应的赛项
            String pj = oPly.getString("u_pj");
            WnObj oPj = io.check(dProj, pj);

            // 得到选手赛项的轨迹信息
            WnObj oPjMap = io.check(oPj, "mars_google.json");
            WoozMap pjMap = io.readJson(oPjMap, WoozMap.class);

            // 循环赛点列表
            for (WoozPoint po : pjMap.points) {
                // 为每个打卡点创建一个打卡记录
            }
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
