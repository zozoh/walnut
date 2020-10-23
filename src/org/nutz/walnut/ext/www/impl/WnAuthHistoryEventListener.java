package org.nutz.walnut.ext.www.impl;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.WnEventListener;
import org.nutz.walnut.api.auth.WnAuthEvent;
import org.nutz.walnut.ext.www.bean.WnWebSite;

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
