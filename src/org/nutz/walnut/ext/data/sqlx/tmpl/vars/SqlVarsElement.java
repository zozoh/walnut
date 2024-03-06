package org.nutz.walnut.ext.data.sqlx.tmpl.vars;

import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.tmpl.ele.TmplEle;

public abstract class SqlVarsElement implements TmplEle {

    private String content;

    protected String[] picks;

    protected String[] omits;

    protected boolean ignoreNil;

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
                // pick=name,color,age
                if (s.startsWith("pick=")) {
                    this.picks = Ws.splitIgnoreBlank(s.substring(5));
                }
                // omit=city,country
                else if (s.startsWith("omit=")) {
                    this.omits = Ws.splitIgnoreBlank(s.substring(5));
                }
                // ignoreNil
                else if (s.equals("ignoreNil")) {
                    this.ignoreNil = true;
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
        if (this.picks != null) {
            bean = context.pick(this.picks);
        } else {
            bean.putAll(context);
        }
        if (this.omits != null) {
            bean = bean.omit(this.omits);
        }
        if (this.ignoreNil) {
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

}
