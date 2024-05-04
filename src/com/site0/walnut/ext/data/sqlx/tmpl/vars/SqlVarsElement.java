package com.site0.walnut.ext.data.sqlx.tmpl.vars;

import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.ele.TmplEle;

public abstract class SqlVarsElement implements TmplEle {

    private String content;

    protected String[] pick;

    protected String[] omit;

    protected Boolean ignoreNil;

    protected String scope;

    protected String defaultValue;

    /**
     * 处理这样的占位符:
     * 
     * <ul>
     * <li><code>${@vars=where; pick=name,color,age; ignoreNil;}</code>
     * <li><code>${@vars=upsert; omit=city,country; ignoreNil;}</code>
     * </ul>
     * 
     * @param content
     *            输入的占位符内容
     * @param expectType
     */
    protected SqlVarsElement(String content) {
        this.content = content;
        if (null != content) {
            String[] ss = Ws.splitIgnoreBlank(content, ";");
            for (String s : ss) {
                // ignoreNil
                if (s.equalsIgnoreCase("ignoreNil")) {
                    this.ignoreNil = true;
                    continue;
                }
                // 解析
                String[] tt = Ws.splitIgnoreBlank(s, "=");
                if (tt.length != 2) {
                    continue;
                }

                String key = tt[0];
                String val = tt[1];

                // scope=abc
                if ("scope".equalsIgnoreCase(key)) {
                    this.scope = val;
                }
                // pick=name,color,age
                else if ("pick".equalsIgnoreCase(key)) {
                    this.pick = Ws.splitIgnoreBlank(val);
                }
                // omit=city,country
                else if ("omit".equalsIgnoreCase(key)) {
                    this.omit = Ws.splitIgnoreBlank(val);
                }
                // dft=xxxx
                else if ("dft".equalsIgnoreCase(key)) {
                    this.defaultValue = val;
                }

                // 错误
                else {
                    throw Er.create("e.sqlx.var.invalid", "'" + s + "' => '${" + content + "}'");
                }
            }
        }

    }

    protected NutBean getBean(NutBean context) {
        NutBean bean = new NutMap();
        if (this.hasScope()) {
            Object obj = context.get(scope);
            context = Wlang.anyToMap(obj);
        }
        if (this.pick != null) {
            bean = context.pick(this.pick);
        } else if (null != context) {
            bean.putAll(context);
        }
        if (this.omit != null) {
            bean = bean.omit(this.omit);
        }
        if (null != this.ignoreNil && this.ignoreNil.booleanValue()) {
            NutBean bean2 = new NutMap();
            for (Map.Entry<String, Object> en : bean.entrySet()) {
                Object val = en.getValue();
                if (null != val) {
                    String key = en.getKey();
                    bean2.put(key, val);
                }
            }
            bean = bean2;
        }
        return bean;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean hasScope() {
        return !Ws.isBlank(scope);
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Boolean getIgnoreNil() {
        return ignoreNil;
    }

    public boolean hasPick() {
        return null != pick;
    }

    public String[] getPick() {
        return pick;
    }

    public void setPick(String[] picks) {
        this.pick = picks;
    }

    public boolean hasOmit() {
        return null != omit;
    }

    public String[] getOmit() {
        return omit;
    }

    public void setOmit(String[] omits) {
        this.omit = omits;
    }

    public boolean hasIgnoreNil() {
        return null != ignoreNil;
    }

    public boolean isIgnoreNil() {
        return ignoreNil;
    }

    public void setIgnoreNil(Boolean ignoreNil) {
        this.ignoreNil = ignoreNil;
    }

    public boolean hasDefaultValue() {
        return !Ws.isBlank(defaultValue);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
