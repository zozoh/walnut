package com.site0.walnut.ext.data.entity.history.hdl;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.ext.data.entity.history.HistoryApi;
import com.site0.walnut.ext.data.entity.history.HistoryRecord;
import com.site0.walnut.ext.data.entity.history.WnHistoryService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;

@JvmHdlParamArgs(value = "cqn", regex = "^(mor)$")
public class history_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备接口
        HistoryApi api = hc.getAs("api", WnHistoryService.class);

        // 准备历史记录
        String json = Cmds.checkParamOrPipe(sys, hc.params, 0);
        NutMap map = Wlang.map(json);
        HistoryRecord his = Wlang.map2Object(map, HistoryRecord.class);
        if (!his.hasCreateTime()) {
            his.setCreateTime(System.currentTimeMillis());
        }

        if (hc.params.is("mor")) {
            String mor = Ws.trim(sys.in.readAll());
            if (!Ws.isBlank(mor)) {
                his.setMore(mor);
            }
        }

        // 补充上关键人员信息
        if (Ws.isBlank(his.getUserId())) {
            WnAccount me = sys.getMe();
            his.setUserId(me.getId());
            his.setUserName(me.getName());
            if (me.isSysAccount()) {
                his.setUserType("domain");
            } else {
                his.setUserType(me.getRoleName());
            }
        }

        // 准备返回值
        Object re = api.add(his);

        // 输出
        json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
