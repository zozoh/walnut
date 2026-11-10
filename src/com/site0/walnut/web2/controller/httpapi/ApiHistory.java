package com.site0.walnut.web2.controller.httpapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

/**
 * RM-NUTZ-09：照抄旧 {@code HttpApiModule._record_history}（1160-1255，
 * Dao 部分旧代码本就是注释 TODO，照抄注释态）。
 */
public class ApiHistory {

    private static final Log log = Wlog.getCMD();

    private final WnIo io;

    public ApiHistory(WnIo io) {
        this.io = io;
    }

    @SuppressWarnings("unchecked")
    public void recordHistory(WnHttpApiContext apc) {
        List<NutMap> historyList = null;
        Object history = apc.oApi.get("history");

        // 防守
        if (null == history)
            return;

        // 动态防守
        Object histest = apc.oApi.get("histest");
        if (null != histest) {
            WnMatch wm = new AutoMatch(histest);
            if (!wm.match(apc.oReq)) {
                return;
            }
        }

        // 多个历史记录
        if (history instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) history;
            historyList = new ArrayList<>(coll.size());

            for (Object it : coll) {
                if (null != it && (it instanceof Map)) {
                    NutMap itMap = NutMap.WRAP((Map<String, Object>) it);
                    historyList.add(itMap);
                }
            }
        }
        // 单条历史记录
        else if (history instanceof Map) {
            NutMap itMap = NutMap.WRAP((Map<String, Object>) history);
            historyList = Wlang.list(itMap);
        }
        // 神马也不是
        else {
            return;
        }

        // 没有历史记录模板，嗯，无视
        if (null == historyList || historyList.isEmpty()) {
            return;
        }

        // 首先，获取一下历史记录的配置文件
        String hisname = apc.oApi.getString("hisname", "_history");
        WnObj oHis = Wn.getObj(io, apc.se, "~/.domain/history/" + hisname + ".json");

        // 木有的话，打印个警告
        if (null == oHis) {
            if (log.isWarnEnabled()) {
                log.warnf("!!! Fail to found history config(%s): %s.json",
                          apc.se.getMyName(),
                          hisname);
            }
            return;
        }

        // TODO 这里需要将历史记录插入到指定的数据表里

        // 生成服务类
        // HistoryConfig conf = WnDaos.loadConfig(HistoryConfig.class, io,
        // oHis, apc.se);
        // Dao dao = WnDaos.get(conf.getAuth());
        // HistoryApi api = new WnHistoryService(conf, dao);
        //
        // // 看看有木有替换的模板
        // List<NutMap> hismetas = apc.oApi.getAsList("hismetas", NutMap.class);
        // NutMap hisUpdate = null;
        // if (null != hismetas) {
        // for (NutMap hismeta : hismetas) {
        // Object test = hismeta.get("test");
        // AutoMatch am = new AutoMatch(test);
        // NutMap update = hismeta.getAs("update", NutMap.class);
        // // 找到了即可
        // if (am.match(apc.oReq)) {
        // hisUpdate = update;
        // break;
        // }
        // }
        // }
        //
        // // 循环添加历史记录
        // for (NutMap hisTmpl : historyList) {
        // // 补充历史记录
        // if (null != hisUpdate)
        // hisTmpl.putAll(hisUpdate);
        //
        // // 解析历史记录
        // NutMap hisre = (NutMap) Wn.explainObj(apc.oReq, hisTmpl);
        //
        // // 插入历史记录
        // HistoryRecord his = Wlang.map2Object(hisre, HistoryRecord.class);
        // api.add(his);
        // }
    }

}
