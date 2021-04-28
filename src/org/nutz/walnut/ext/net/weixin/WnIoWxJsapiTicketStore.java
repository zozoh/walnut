package org.nutz.walnut.ext.net.weixin;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.weixin.at.WxJsapiTicket;
import org.nutz.weixin.spi.WxJsapiTicketStore;

public class WnIoWxJsapiTicketStore implements WxJsapiTicketStore {

    private WnIo io;
    private WnObj home;

    public WnIoWxJsapiTicketStore(WnIo io, WnObj home) {
        this.io = io;
        this.home = home;
    }

    @Override
    public WxJsapiTicket get() {
        WnObj oAt = io.fetch(home, "jsapi_ticket");
        if (null != oAt) {
            return new WxJsapiTicket(oAt.getString("wx_jt_ticket"),
                                     oAt.getInt("wx_jt_expires"),
                                     oAt.getLong("wx_jt_lm"));
        }
        return null;
    }

    @Override
    public void save(String ticket, int expires, long lastCacheTimeMillis) {
        WnObj oAt = io.createIfNoExists(home, "jsapi_ticket", WnRace.FILE);
        oAt.setv("wx_jt_ticket", ticket);
        oAt.setv("wx_jt_expires", expires);
        oAt.setv("wx_jt_lm", lastCacheTimeMillis);
        io.set(oAt, "^wx_jt_.*$");
    }

}
