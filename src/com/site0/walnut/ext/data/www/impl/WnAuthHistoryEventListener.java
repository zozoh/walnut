package com.site0.walnut.ext.data.www.impl;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.WnEventListener;
import com.site0.walnut.api.auth.WnAuthEvent;
import com.site0.walnut.ext.data.www.bean.WnWebSite;

public class WnAuthHistoryEventListener implements WnEventListener<WnAuthEvent> {

    private WnWebSite site;

    public WnAuthHistoryEventListener(WnWebSite site) {
        this.site = site;
    }

    @Override
    public void invoke(String eventName, WnAuthEvent event) {
        NutBean context = event.toBean();
        site.addHistoryRecord(context, eventName);
    }

}
