package org.nutz.walnut.util.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.bean.util.WnBeanFieldMatchValue;
import org.nutz.walnut.util.bean.val.WnValueType;

public class WnValue {

    private WnValueType type;

    private WnValue eleType;

    private WnBeanMapping mapping;

    private boolean mappingOnly;

    private Object defaultAs;

    private Object emptyAs;

    /**
     * 譬如 xsls 的日期，通常类型为 "44495"，表示 1900-01-01 之后的 44495 天 <br>
     * 这种，声明前缀 "=1900+" 则表示这个日期类型是 "=1900+44495" 也就是 "2021-10-26"
     */
    private String datePrefix;

    private String format;

    private String[] replace;

    private String valueRegion;

    private String separator;

    private NutMap values;

    private WnBeanFieldMatchValue[] matchValue;

    private WnEnumOptionItem[] options;

    private String optionsFile;

    private String optionsFromKey;

    private String optionsToKey;

    private Map<String, Object> __options_map;

    public WnValue() {
        this.setType(WnValueType.String);
    }

    public Object tryValueOptions(Object input) {
        if (null != input) {
            String val = input.toString();
            if (null != values) {
                Object v2 = this.values.get(val);
                if (null != v2) {
                    return v2;
                }
            }
            Object v3 = this.getOptionValue(val, input);
            if (null != v3) {
                return v3;
            }
            return input;
        }
        return input;
    }

    public Object getOptionValue(String text, Object dft) {
        if (null != __options_map) {
            Object v = __options_map.get(text);
            if (null == v) {
                return dft;
            }
            return v;
        }
        return dft;
    }

    public void loadOptions(WnIo io, NutBean vars, Map<String, NutMap[]> caches) {
        if (null == this.__options_map) {
            __options_map = new HashMap<>();
            // 一个静态值
            if (null != options) {
                for (WnEnumOptionItem it : options) {
                    __options_map.put(it.getText(), it.getValue());
                }
            }
            // 动态从文件获取
            else if (null != this.optionsFile) {
                String aph = Wn.normalizeFullPath(this.optionsFile, vars);
                NutMap[] list = caches.get(aph);
                // 直接读取并加入缓存
                if (null == list) {
                    WnObj oFile = io.check(null, aph);
                    list = io.readJson(oFile, NutMap[].class);
                    caches.put(aph, list);
                }
                // 准备映射
                String fromKey = Ws.sBlank(this.optionsFromKey, "text");
                String toKey = Ws.sBlank(this.optionsToKey, "value");
                for (NutMap li : list) {
                    String from = li.getString(fromKey);
                    Object to = li.get(toKey);
                    __options_map.put(from, to);
                }
            }
        }
    }

    public WnValueType getType() {
        return type;
    }

    public void setType(WnValueType type) {
        this.type = type;
    }

    public WnValue getEleType() {
        return eleType;
    }

    public void setEleType(WnValue element) {
        this.eleType = element;
    }

    public boolean hasMapping() {
        return null != this.mapping && !this.mapping.isEmpty();
    }

    public WnBeanMapping getMapping() {
        return mapping;
    }

    public void setMapping(WnBeanMapping eleMapping) {
        this.mapping = eleMapping;
    }

    public boolean isMappingOnly() {
        return mappingOnly;
    }

    public void setMappingOnly(boolean eleMappingOnly) {
        this.mappingOnly = eleMappingOnly;
    }

    public Object getDefaultAs() {
        return defaultAs;
    }

    public void setDefaultAs(Object defaultAs) {
        this.defaultAs = defaultAs;
    }

    public Object getEmptyAs() {
        return emptyAs;
    }

    public void setEmptyAs(Object emptyAs) {
        this.emptyAs = emptyAs;
    }

    public String getDatePrefix() {
        return datePrefix;
    }

    public void setDatePrefix(String datePrefix) {
        this.datePrefix = datePrefix;
    }

    public boolean hasReplace() {
        return null != replace && replace.length > 0;
    }

    public String[] getReplace() {
        return replace;
    }

    public void setReplace(String[] replace) {
        this.replace = replace;
    }

    public boolean hasFormat() {
        return null != format;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean hasValueRegion() {
        return !Ws.isBlank(this.valueRegion);
    }

    public boolean isIntInRegion(int v) {
        if (null == this.valueRegion) {
            return true;
        }
        Region<Integer> rg = Region.Int(this.valueRegion);
        return rg.match(v);
    }

    public boolean isLongInRegion(long v) {
        if (null == this.valueRegion) {
            return true;
        }
        Region<Long> rg = Region.Long(this.valueRegion);
        return rg.match(v);
    }

    public boolean isFloatInRegion(float v) {
        if (null == this.valueRegion) {
            return true;
        }
        Region<Float> rg = Region.Float(this.valueRegion);
        return rg.match(v);
    }

    public boolean isDoubleInRegion(double v) {
        if (null == this.valueRegion) {
            return true;
        }
        Region<Double> rg = Region.Double(this.valueRegion);
        return rg.match(v);
    }

    public boolean isDateInRegion(Date v) {
        if (null == this.valueRegion) {
            return true;
        }
        Region<Date> rg = Region.Date(this.valueRegion);
        return rg.match(v);
    }

    public String getValueRegion() {
        return valueRegion;
    }

    public void setValueRegion(String valueRegion) {
        this.valueRegion = valueRegion;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public Object tryMatchValue(NutBean bean) {
        if (this.hasMatchValue()) {
            for (WnBeanFieldMatchValue bfmv : this.matchValue) {
                if (bfmv.matchTest(bean)) {
                    return bfmv.getValue();
                }
            }
        }
        return null;
    }

    public boolean hasMatchValue() {
        return null != matchValue && matchValue.length > 0;
    }

    public WnBeanFieldMatchValue[] getMatchValue() {
        return matchValue;
    }

    public void setMatchValue(WnBeanFieldMatchValue[] matchmatchValue) {
        this.matchValue = matchmatchValue;
    }

    public boolean hasValues() {
        return null != values && !values.isEmpty();
    }

    public NutMap getValues() {
        return values;
    }

    public void setValues(NutMap values) {
        this.values = values;
    }

    public WnEnumOptionItem[] getOptions() {
        return options;
    }

    public void setOptions(WnEnumOptionItem[] options) {
        this.options = options;
    }

    public String getOptionsFile() {
        return optionsFile;
    }

    public void setOptionsFile(String optionsFile) {
        this.optionsFile = optionsFile;
    }

    public String getOptionsFromKey() {
        return optionsFromKey;
    }

    public void setOptionsFromKey(String optionsFromKey) {
        this.optionsFromKey = optionsFromKey;
    }

    public String getOptionsToKey() {
        return optionsToKey;
    }

    public void setOptionsToKey(String optionsToKey) {
        this.optionsToKey = optionsToKey;
    }

}
