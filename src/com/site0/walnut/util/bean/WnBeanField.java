package com.site0.walnut.util.bean;

import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class WnBeanField extends WnValue {

    private Object visible;

    private Object hidden;

    private String name;
    
    private boolean ignoreNull;

    /**
     * 一个字段还可以转换为几种变种。
     * <p>
     * 这个设计，主要是一个整型，譬如<code>2</cod>，我想让转换出来的对象有个<code>"02"</code>这样的字段值。
     * <p>
     * <b>!!!注</b> 别名字段的转换输入，是主字段的输出。即，主字段经过了 options 枚举转换，并转型到了期望的值<br>
     * 这样做的好处，是能有一个可预期的统一的输入，譬如 "%02d"这种转换，对方来个字符串，你就挂了。
     */
    private WnBeanField[] aliasFields;

    /**
     * 如果自己是别名字段（AliasField），本属性才有用。
     * <p>
     * 表示，自己的【源值】不再是原始值，而是主字段转换后的值
     */
    private boolean useMappedValue;

    private WnMatch __visible_match;

    public WnMatch getVisibleMatch() {
        if (this.__visible_match == null) {
            if (null != this.visible) {
                this.__visible_match = new AutoMatch(this.visible);
            }
        }
        return this.__visible_match;
    }

    public Object getVisible() {
        return visible;
    }

    public void setVisible(Object visible) {
        this.visible = visible;
    }

    private WnMatch __hidden_match;

    public WnMatch getHiddenMatch() {
        if (this.__hidden_match == null) {
            if (null != this.hidden) {
                this.__hidden_match = new AutoMatch(this.hidden);
            }
        }
        return this.__hidden_match;
    }

    public Object getHidden() {
        return hidden;
    }

    public void setHidden(Object hidden) {
        this.hidden = hidden;
    }

    public boolean isIgnore(NutBean bean) {
        WnMatch m = this.getHiddenMatch();
        if (null != m && m.match(bean)) {
            return true;
        }
        m = this.getVisibleMatch();
        if (null != m) {
            return !m.match(bean);
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public String getName(String dftName) {
        return Ws.sBlank(name, dftName);
    }

    public void setName(String name) {
        this.name = name;
    }
    
    

    public boolean isIgnoreNull() {
        return ignoreNull;
    }

    public void setIgnoreNull(boolean ignoreNull) {
        this.ignoreNull = ignoreNull;
    }

    public void loadOptions(WnIo io, NutBean vars, Map<String, NutMap[]> caches) {
        super.loadOptions(io, vars, caches);
        if (this.hasAliasFields()) {
            for (WnBeanField af : this.aliasFields) {
                af.loadOptions(io, vars, caches);
            }
        }
    }

    public boolean hasAliasFields() {
        return null != aliasFields && aliasFields.length > 0;
    }

    public WnBeanField[] getAliasFields() {
        return aliasFields;
    }

    public void setAliasFields(WnBeanField[] aliasFields) {
        this.aliasFields = aliasFields;
    }

    public boolean isUseMappedValue() {
        return useMappedValue;
    }

    public void setUseMappedValue(boolean useMappedValue) {
        this.useMappedValue = useMappedValue;
    }

}
