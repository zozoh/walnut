package org.nutz.walnut.util.bean;

import org.nutz.walnut.util.Ws;

public class WnBeanField extends WnValue {

    private String name;

    /**
     * 一个字段还可以转换为几种变种。
     * <p>
     * 这个设计，主要是一个整型，譬如<code>2</cod>，我想让转换出来的对象有个<code>"02"</code>这样的字段值。
     * <p>
     * <b>!!!注</b> 别名字段的转换输入，是主字段的输出。即，主字段经过了 options 枚举转换，并转型到了期望的值<br>
     * 这样做的好处，是能有一个可预期的统一的输入，譬如 "%02d"这种转换，对方来个字符串，你就挂了。
     */
    private WnBeanField[] aliasFields;

    public String getName() {
        return name;
    }

    public String getName(String dftName) {
        return Ws.sBlank(name, dftName);
    }

    public void setName(String name) {
        this.name = name;
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

}
