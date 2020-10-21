package org.nutz.walnut.ext.www.impl;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnEventListener;
import org.nutz.walnut.api.auth.WnAuthEvent;
import org.nutz.walnut.ext.entity.history.HistoryApi;
import org.nutz.walnut.ext.entity.history.HistoryRecord;
import org.nutz.walnut.ext.www.bean.WnWebSite;
import org.nutz.walnut.util.Wn;

public class WnAuthHistoryEventListener implements WnEventListener<WnAuthEvent> {

    private WnWebSite site;

    private NutBean hisTmpl;

    public WnAuthHistoryEventListener(WnWebSite site, NutBean hisTmpl) {
        this.site = site;
        this.hisTmpl = hisTmpl;
    }

    @Override
    public void invoke(String eventName, WnAuthEvent event) {
        HistoryApi api = site.getHistoryApi();
        NutBean context = event.toBean();
        NutMap hisre = (NutMap) Wn.explainObj(context, hisTmpl);

        // 插入历史记录
        HistoryRecord his = Lang.map2Object(hisre, HistoryRecord.class);
        api.add(his);
    }

}
