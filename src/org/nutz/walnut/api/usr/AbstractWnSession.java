package org.nutz.walnut.api.usr;

import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;

public abstract class AbstractWnSession implements WnSession {

    private static Pattern Session_Key_Reserved_Pattern = Pattern.compile("^(passwd|salt|pid|ct|lm|data|sha1|len|c|g|m|d0|d1|md|tp|mime|ph)$");

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
    public NutMap putUsrVars(WnUsr u) {
        NutMap vars = this.vars();
        vars.put("PWD", u.home());
        for (Map.Entry<String, Object> en : u.entrySet()) {
            String key = en.getKey();
            // 双下划线开始的元数据无视
            if (key.startsWith("__"))
                continue;
            // 其他不显示的键
            if (Session_Key_Reserved_Pattern.matcher(key).matches()) {
                continue;
            }
            // HOME 特殊处理
            if ("home".equals(key)) {
                vars.setv("HOME", en.getValue());
            }
            // 如果是大写的变量，则全部保留，比如 "PATH" 或者 "APP-PATH"
            else if (key.toUpperCase().equals(key)) {
                vars.setv(key, en.getValue());
            }
            // 如果是 my_ 开头变量，仅仅是变大写
            else if (key.startsWith("my_")) {
                vars.setv(key.toUpperCase(), en.getValue());
            }
            // 其他加前缀
            else {
                vars.setv("MY_" + key.toUpperCase(), en.getValue());
            }
        }
        return vars;
    }

    @Override
    public boolean hasVar(String nm) {
        return null != var(nm);
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
