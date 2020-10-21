package org.nutz.walnut.ext.sql;

import org.nutz.lang.util.NutMap;

public class WnDaoFieldsConfig extends WnDaoConfig {

    private NutMap fieldSizes;

    public NutMap getFieldSizes() {
        return fieldSizes;
    }

    public void setFieldSizes(NutMap fieldSizes) {
        this.fieldSizes = fieldSizes;
    }

    public String truncate(String key, String input) {
        if (null == input || null == fieldSizes)
            return input;

        int len = fieldSizes.getInt(key, 0);
        if (len > 0 && input.length() > len) {
            return input.substring(0, len);
        }

        return input;
    }

}
