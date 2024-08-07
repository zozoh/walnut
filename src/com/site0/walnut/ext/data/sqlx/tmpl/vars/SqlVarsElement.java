package com.site0.walnut.ext.data.sqlx.tmpl.vars;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
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
                String key, val;

                if (tt.length == 2) {
                    key = tt[0];
                    val = tt[1];
                } else {
                    key = tt[0];
                    val = null;
                }

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
                else if (!acceptSetup(key, val)) {
                    throw Er.create("e.sqlx.var.invalid", "'" + s + "' => '${" + content + "}'");
                }
            }
        }

    }

    /**
     * 子类重载这个函数，可以支持更多的自定义配置项目
     * 
     * @param key
     *            配置名
     * @param val
     *            配置值
     * @return 是否接受这个配置
     */
    protected boolean acceptSetup(String key, String val) {
        return false;
    }

    private NutBean __apply_ignore_nil(NutBean bean) {
        NutBean bean2 = new NutMap();
        for (Map.Entry<String, Object> en : bean.entrySet()) {
            Object val = en.getValue();
            if (null != val) {
                String key = en.getKey();
                bean2.put(key, val);
            }
        }
        bean = bean2;
        return bean;
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
            bean = __apply_ignore_nil(bean);
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    protected Object getObject(NutBean context) {
        Object val = context;
        if (this.hasScope()) {
            val = context.get(scope);
        }

        // 对于单个对象
        if (val instanceof Map) {
            return __apply_bean((Map<String, Object>) val);
        }
        // 对于集合
        if (val instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) val;
            List<Object> list = new ArrayList<>(coll.size());
            for (Object it : coll) {
                if (it instanceof Map) {
                    list.add(__apply_bean((Map<String, Object>) it));
                } else {
                    list.add(it);
                }
            }
            return list;
        }

        return val;
    }

    private Object __apply_bean(Map<String, Object> val) {
        NutBean bean = NutMap.WRAP(val);
        if (this.pick != null && pick.length > 0) {
            bean = bean.pick(pick);
        }
        if (this.omit != null && omit.length > 0) {
            bean = bean.omit(omit);
        }
        if (null != this.ignoreNil && this.ignoreNil.booleanValue()) {
            bean = __apply_ignore_nil(bean);
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
