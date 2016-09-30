package org.nutz.walnut.api.usr;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;

public abstract class AbstractWnSession implements WnSession {

    public WnSession clone() {
        throw Lang.noImplement();
    }

    @Override
    public boolean isSame(String seid) {
        return id().equals(seid);
    }

    @Override
    public boolean isSame(WnSession se) {
        return id().equals(se.id());
    }

    @Override
    public boolean isParentOf(WnSession se) {
        return se.hasParentSession() && this.isSame(se.getParentSessionId());
    }

    @Override
    public boolean hasParentSession() {
        return !Strings.isBlank(this.getParentSessionId());
    }

    @Override
    public String varString(String nm) {
        Object v = var(nm);
        return null == v ? null : v.toString();
    }

    @Override
    public int varInt(String nm) {
        return varInt(nm, Integer.MIN_VALUE);
    }

    @Override
    public int varInt(String nm, int dft) {
        Object v = var(nm);
        return null == v ? dft : Castors.me().castTo(v, Integer.class);
    }

    @Override
    public NutMap toMapForClient() {
        NutMap map = new NutMap();
        map.put("id", this.id());
        map.put("p_se_id", this.getParentSessionId());
        map.put("me", this.me());
        map.put("grp", this.group());
        map.put("du", this.duration());
        map.put("envs", this.vars());
        return map;
    }

    @Override
    public String toJson(JsonFormat jfmt) {
        return Json.toJson(this.toMapForClient(), jfmt);
    }

    public String toString() {
        return String.format("%s>%s:%s/%s[%ds] to %s",
                             Strings.sBlank(this.getParentSessionId(), ""),
                             this.id(),
                             this.me(),
                             this.group(),
                             this.duration() / 1000,
                             Times.sDT(Times.D(this.expireTime())));
    }
}
