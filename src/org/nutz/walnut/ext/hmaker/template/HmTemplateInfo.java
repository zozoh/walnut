package org.nutz.walnut.ext.hmaker.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutMap;

public class HmTemplateInfo {

    public String name;

    public String title;

    public String dataType;

    public NutMap options;

    public NutMap dom;

    public String getDomFileName(String dft) {
        if (null != dom)
            dom.getString("fileName", dft);
        return dft;
    }

    public String getDomVarName(String dft) {
        if (null != dom)
            return dom.getString("varName", dft);
        return dft;
    }

    public HmTemplateInfo evalOptions() {
        if (null == options || options.size() == 0)
            return this;

        // 开始迭代
        for (Map.Entry<String, Object> en : options.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if (null != val) {
                HmTmplField v2 = new HmTmplField().valueBy(val).setKey(key);
                en.setValue(v2);
            }
        }

        return this;
    }

    public List<String> getFieldByType(String type) {
        List<String> list = new ArrayList<>(options.size());
        for (Map.Entry<String, Object> en : options.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if (null != val) {
                HmTmplField v2 = HmTmplField.eval(val);
                if (v2.isType(type))
                    list.add(key);
            }
        }
        return list;
    }

}
