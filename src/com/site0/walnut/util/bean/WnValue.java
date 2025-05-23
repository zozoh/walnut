package com.site0.walnut.util.bean;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.nutz.lang.Each;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.bean.util.WnBeanFieldMatchValue;
import com.site0.walnut.util.bean.val.WnValueType;
import com.site0.walnut.util.tmpl.WnTmplX;
import com.site0.walnut.util.validate.WnMatch;

public class WnValue {

    private WnValueType type;

    private WnValue eleType;

    private WnBeanMapping mapping;

    private boolean mappingOnly;

    private Object defaultAs;

    private Object emptyAs;

    /**
     * 如果值是一个数字，需要将其除以这个单位。 从而表示一个浮点数。 譬如数据库里存的是分 896，如果设置 unit 100 则会输出为 8.96
     * 如果再配合上 format="%.2fRMB" 则会输出为 '8.96RMB'
     */
    private int unit;

    /**
     * 譬如 xsls 的日期，通常类型为 "44495"，表示 1900-01-01 之后的 44495 天 <br>
     * 这种，声明前缀 "=1900+" 则表示这个日期类型是 "=1900+44495" 也就是 "2021-10-26"
     */
    private String datePrefix;

    private String format;

    /**
     * 指定转换时，读取源数据（如果是字符串）到标准日期对象时，需要采用的时区
     * <p>
     * 如果不指定，则采用线程上下文时区
     * 
     * 
     * 如果类型为 `Date` 相关的字段映射，格式化时，可以指定时区，譬如:
     * 
     * <ul>
     * <li><code>US/Arizona</code> 北美山区标准时间
     * <li><code>Antarctica/Macquarie</code> 澳大利亚东部标准时间
     * <li><code>Asia/Shanghai</code> 中国标准时间
     * <li><code>GMT+8</code> 直接写时区偏移
     * <li><code>UTC</code> 协调世界时
     * </ul>
     * 
     * 更多时区信息，可以通过 <code>datex @timezone -list</code>查看
     */
    private String fromTimeZoneId;

    /**
     * 指定转换时，输出目标日期时间字符串，需要采用的时区.
     * <p>
     * 如果不指定，则采用线程上下文时区
     */
    private String toTimeZoneId;

    /**
     * 输出的时候用字符串模板转换,模板的上下文就是整个 Bean
     */
    private String tmpl;

    private WnTmplX _tmpl;

    /**
     * 如果是字符串型函数，支持转换大小写等CASE。
     * 
     * <ul>
     * <li><code>camel</code> : 单词首字母大写
     * <li><code>kebab</code> : 中划线分隔
     * <li><code>snake</code> : 下划线分隔
     * <li><code>upper</code> : 全大写
     * <li><code>lower</code> : 全小写
     * </ul>
     */
    private String valueCase;

    private String[] replace;

    private String valueRegion;

    private String separator;

    private String split;

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

    @SuppressWarnings("unchecked")
    public Object tryValueOptions(Object input) {
        if (null == input) {
            return null;
        }
        // 需要将字符串自动变成数组
        if ((input instanceof String) && !Ws.isEmpty(this.split)) {
            input = Ws.splitIgnoreBlank(input.toString(), this.split);
        }
        // 集合对象
        if (input instanceof Collection<?>) {
            Collection<Object> col = (Collection<Object>) input;
            List<Object> re = new ArrayList<>(col.size());
            for (Object ele : col) {
                Object v2 = this.__get_option_val(ele);
                re.add(v2);
            }
            return re;
        }
        // 数组对象
        if (input.getClass().isArray()) {
            int len = Array.getLength(input);
            Object[] re = (Object[]) Array.newInstance(Object.class, len);
            for (int i = 0; i < len; i++) {
                Object ele = Array.get(input, i);
                Object v2 = this.__get_option_val(ele);
                re[i] = v2;
            }
            return re;
        }
        // 其他对象统统字符串之
        return __get_option_val(input);
    }

    protected Object __get_option_val(Object input) {
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
                NutMap[] items = caches.get(aph);
                // 直接读取并加入缓存
                if (null == items) {
                    WnObj oFile = io.check(null, aph);
                    // 如果是 JSON 文件
                    if (oFile.isFILE() && oFile.isType("json")) {
                        items = io.readJson(oFile, NutMap[].class);
                    }
                    // 如果是一个 ThingSet
                    else if (oFile.isDIR() && oFile.isType("thing_set")) {
                        WnObj oIndex = io.fetch(oFile, "index");
                        if (null == oIndex) {
                            items = new NutMap[0];
                        } else {
                            items = loadOptionsFromDir(io, oIndex);
                        }
                    }
                    // 如果就是普通目录
                    else if (oFile.isDIR()) {
                        items = loadOptionsFromDir(io, oFile);
                    }
                    // 靠，啥也不是
                    else {
                        items = new NutMap[0];
                    }
                    caches.put(aph, items);
                }
                // 准备映射
                String fromKey = Ws.sBlank(this.optionsFromKey, "text");
                String toKey = Ws.sBlank(this.optionsToKey, "value");
                for (NutMap li : items) {
                    String from = li.getString(fromKey);
                    Object to = li.get(toKey);
                    __options_map.put(from, to);
                }
            }
        }
    }

    private NutMap[] loadOptionsFromDir(WnIo io, WnObj oDir) {
        NutMap[] items;
        List<NutMap> list = new LinkedList<>();

        // 过滤字段
        WnMatch ma = Wobj.explainObjKeyMatcher("#NM");

        // 查询
        WnQuery q = Wn.Q.pid(oDir);
        q.limit(2000);
        io.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj obj, int length) {
                NutMap bean = (NutMap) Wobj.filterObjKeys(obj, ma);
                list.add(bean);
            }
        });

        // 输出
        items = list.toArray(new NutMap[list.size()]);
        return items;
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

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
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

    public boolean hasTmpl() {
        return null != this.tmpl;
    }

    public String getTmpl() {
        return tmpl;
    }

    public void setTmpl(String tmpl) {
        this.tmpl = tmpl;
        if (null != tmpl) {
            this._tmpl = WnTmplX.parse(tmpl);
        } else {
            this._tmpl = null;
        }
    }

    private TimeZone _from_time_zone;

    public boolean hasFromTimeZone() {
        return null != _from_time_zone;
    }

    public TimeZone getFromTimeZone() {
        return this._from_time_zone;
    }

    private TimeZone _to_time_zone;

    public boolean hasToTimeZone() {
        return null != this._to_time_zone;
    }

    public TimeZone getToTimeZone() {
        return this._to_time_zone;
    }

    public String getFromTimeZoneId() {
        return fromTimeZoneId;
    }

    public void setFromTimeZoneId(String fromTimeZoneId) {
        this.fromTimeZoneId = fromTimeZoneId;
        if (null == fromTimeZoneId) {
            this._from_time_zone = null;
        } else {
            this._from_time_zone = TimeZone.getTimeZone(fromTimeZoneId);
        }
    }

    public String getToTimeZoneId() {
        return toTimeZoneId;
    }

    public void setToTimeZoneId(String toTimeZoneId) {
        this.toTimeZoneId = toTimeZoneId;
        if (null == toTimeZoneId) {
            this._to_time_zone = null;
        } else {
            this._to_time_zone = TimeZone.getTimeZone(toTimeZoneId);
        }
    }

    public WnTmplX getTemplate() {
        return this._tmpl;
    }

    public boolean hasValueCase() {
        return null != this.valueCase;
    }

    public String getValueCase() {
        return valueCase;
    }

    public void setValueCase(String valueCase) {
        this.valueCase = valueCase;
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

    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
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
