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

    private String id;

    private WnObj me;

    private String ticket;

    private long expi;

    private String byType;

    private String byValue;

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
        this.byType = oSe.getString("by_tp");
        this.byValue = oSe.getString("by_val");
    }

    public NutMap toMeta() {
        NutMap map = new NutMap();
        if (null != me) {
            map.put("uid", me.id());
            map.put("unm", me.name());
        }
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

    public String getByType() {
        return byType;
    }

    public String getByValue() {
        return byValue;
    }

    public NutBean getUserInfo() {
        if (null == this.me) {
            return new NutMap();
        }
        String locked = "^(id|nm|ph|race|tp|mime|pid|len|sha1|ct|lm|"
                        + "c|m|g|md|ph|th_set|th_live|d0|d1||passwd|salt)$";
        return this.me.pickBy(Pattern.compile(locked), true);
    }

    public String formatJson(JsonFormat jfmt, boolean ajax) {
        jfmt.setLocked("^(id|ph|race|tp|mime|pid|len|sha1|ct|lm|c|m|g|md|ph|th_set|th_live|"
                       + "d0|d1||passwd|salt|oauth_.+|wx_.+)$");
        jfmt.setIgnoreNull(true);
        if (ajax) {
            AjaxReturn re = Ajax.ok().setData(this);
            return Json.toJson(re, jfmt);
        }
        return Json.toJson(this, jfmt);
    }

}