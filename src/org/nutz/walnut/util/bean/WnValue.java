package org.nutz.walnut.util.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.util.Region;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.bean.val.WnValueType;

public class WnValue {

    private WnValueType type;

    private WnValue eleType;

    private WnBeanMapping mapping;

    private boolean mappingOnly;

    private Object defaultAs;

    private Object emptyAs;

    private String format;

    private String valueRegion;

    private String separator;

    private WnEnumOptionItem[] options;

    private Map<String, Object> __options_map;

    public WnValue() {
        this.setType(WnValueType.String);
    }

    public Object tryValueOptions(Object input) {
        if (null != input && null != options) {
            String val = input.toString();
            return this.getOptionValue(val, input);
        }
        return input;
    }

    public Object getOptionValue(String text, Object dft) {
        if (null != options) {
            if (null == __options_map) {
                __options_map = new HashMap<>();
                for (WnEnumOptionItem it : options) {
                    __options_map.put(it.getText(), it.getValue());
                }
            }
            Object v = __options_map.get(text);
            if (null == v) {
                return dft;
            }
            return v;
        }
        return dft;
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

    public WnEnumOptionItem[] getOptions() {
        return options;
    }

    public void setOptions(WnEnumOptionItem[] options) {
        this.options = options;
    }

}
