package org.nutz.walnut.ext.www.bean;

import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.json.JsonIgnore;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

public class WnWebSession {

    private static final String LOCKED_U_FIELDS = "^(id|race|tp|mime|pid|len|sha1|ct|lm|c|m|g|md|ph|th_set|th_live|d0|d1||passwd|salt|oauth_.+|wx_.+)$";

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

    public String getUserId() {
        return me.id();
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

    public NutBean getUserInfo() {
        return this.me.pickBy(Pattern.compile(LOCKED_U_FIELDS), true);
    }

    public String formatJson(JsonFormat jfmt, boolean ajax) {
        jfmt.setLocked(LOCKED_U_FIELDS);
        jfmt.setIgnoreNull(true);
        if (ajax) {
            AjaxReturn re = Ajax.ok().setData(this);
            return Json.toJson(re, jfmt);
        }
        return Json.toJson(this, jfmt);
    }

}
