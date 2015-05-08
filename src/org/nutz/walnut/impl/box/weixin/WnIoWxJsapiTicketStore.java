package org.nutz.walnut.impl.box.weixin;

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
        WnObj oAt = io.fetch(home, "token");
        if (null != oAt) {
            WxJsapiTicket wat = new WxJsapiTicket();
            wat.setTicket(oAt.getString("wx_jt_ticket"));
            wat.setExpires(oAt.getInt("wx_jt_expires"));
            return wat;
        }
        return null;
    }

    @Override
    public void save(String ticket, int expires) {
        WnObj oAt = io.createIfNoExists(home, "jsapi_ticket", WnRace.FILE);
        oAt.setv("wx_jt_ticket", ticket);
        oAt.setv("wx_jt_expires", System.currentTimeMillis() / 1000 + expires);
        io.appendMeta(oAt, "^wx_jt_.*$");
    }

}
