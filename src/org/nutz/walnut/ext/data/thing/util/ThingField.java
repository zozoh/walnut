package org.nutz.walnut.ext.data.thing.util;

import org.nutz.json.JsonIgnore;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.validate.NutValidate;
import org.nutz.validate.NutValidateException;
import org.nutz.walnut.api.err.Er;

public class ThingField {

    private String key;

    // private boolean required;

    // private boolean virtual;

    // private String type;

    private NutMap validate;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    // public boolean isRequired() {
    // return required;
    // }
    //
    // public void setRequired(boolean required) {
    // this.required = required;
    // }

    // public boolean isVirtual() {
    // return virtual;
    // }
    //
    // public void setVirtual(boolean virtual) {
    // this.virtual = virtual;
    // }

    // public String getType() {
    // return type;
    // }
    //
    // public void setType(String type) {
    // this.type = type;
    // }

    // public boolean isObject() {
    // return "object".equals(type);
    // }
    //
    // public boolean isDateRange() {
    // return "daterange".equals(type);
    // }
    //
    // public boolean isDateTime() {
    // return "datetime".equals(type);
    // }
    //
    // public boolean isTime() {
    // return "time".equals(type);
    // }
    //
    // public boolean isInt() {
    // return "int".equals(type);
    // }
    //
    // public boolean isFloat() {
    // return "float".equals(type);
    // }
    //
    // public boolean isBoolean() {
    // return "boolean".equals(type);
    // }

    public NutMap getValidate() {
        return validate;
    }

    public void setValidate(NutMap validate) {
        this.validate = validate;
    }

    @JsonIgnore
    private NutValidate __validate;

    public void validate(NutBean meta) {
        // if (this.virtual)
        // return;

        // 自定义检查器
        if (null == this.validate || validate.isEmpty())
            return;
        // 懒加载
        if (null == this.__validate) {
            this.__validate = new NutValidate(this.validate);
        }

        // 执行检查
        Object val = meta.get(key);

        try {
            Object v2 = this.__validate.check(val);
            if (val != v2) {
                meta.put(key, v2);
            }
        }
        catch (NutValidateException e) {
            throw Er.createf("e.cmd.thing.invalid", "Key:[%s]:: %s", key, e.toString());
        }
    }

}
