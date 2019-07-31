package org.nutz.walnut.ext.www.bean;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;

public class WnWebSession {

    private String id;

    private WnObj me;

    private String ticket;

    private long expi;
    
    public WnWebSession(String ticket) {
        this.ticket = ticket;
    }

    public WnWebSession(WnObj oSe, WnObj oMe) {
        String uid = oSe.getString("uid");
        if (!oMe.isSameId(uid)) {
            throw Er.create("e.www.session.nomatched", uid);
        }
        this.id = oSe.id();
        this.me = oMe;
        this.ticket = oSe.name();
        this.expi = oSe.expireTime();
    }
    
    public NutMap toMeta() {
        NutMap map = new NutMap();
        map.put("uid", me.id());
        map.put("unm", me.name());
        map.put("expi", expi);
        return map;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WnObj getMe() {
        return me;
    }

    public void setMe(WnObj me) {
        this.me = me;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public long getExpi() {
        return expi;
    }

    public void setExpi(long expi) {
        this.expi = expi;
    }

}
