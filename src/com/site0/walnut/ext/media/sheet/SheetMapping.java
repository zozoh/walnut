package com.site0.walnut.ext.media.sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.meta.Pair;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.bean.WnBeanField;
import com.site0.walnut.util.bean.WnBeanMapping;
import com.site0.walnut.util.each.WnBreakException;
import com.site0.walnut.util.validate.WnMatch;

public class SheetMapping {

    private WnMatch keys;

    private WnMatch names;

    private WnMatch omit;

    private WnMatch pick;

    private int limit;

    private int skip;

    private List<SheetField> fields;

    private WnBeanMapping beanMapping;

    public SheetMapping() {
        this.limit = 0;
        this.skip = 0;
    }

    public WnMatch getKeys() {
        return keys;
    }

    public void setKeys(WnMatch keys) {
        this.keys = keys;
    }

    public WnMatch getNames() {
        return names;
    }

    public void setNames(WnMatch names) {
        this.names = names;
    }

    public WnMatch getOmit() {
        return omit;
    }

    public void setOmit(WnMatch omit) {
        this.omit = omit;
    }

    public WnMatch getPick() {
        return pick;
    }

    public void setPick(WnMatch pick) {
        this.pick = pick;
    }

    public WnBeanMapping getBeanMapping() {
        return beanMapping;
    }

    public void setBeanMapping(WnBeanMapping beanMapping) {
        this.beanMapping = beanMapping;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public List<SheetField> getFields() {
        return fields;
    }

    public List<String> getHeadKeys() {
        // 指定了映射: WnBeanMapping
        if (null != this.beanMapping) {
            /*
             * 因为 isKeyCanOutput 判断的是 fld.name， export 的时候，这个name
             * 是中文，什么的匹配不到，但是又必须输出header， 就自动判断第一行有的值的键了，改成用 MapEntry.key来判断才好
             */
            List<String> beanKeys = new ArrayList<>(beanMapping.size());
            for (Map.Entry<String, WnBeanField> en : beanMapping.entrySet()) {
                // 自己的键

                String k = en.getKey();
                if (!isKeyCanOutput(k)) {
                    continue;
                }
                WnBeanField fld = en.getValue();
                String fldName = fld.getName();
                if (fldName == null || fldName.matches("^[-]{5,}$")) {
                    continue;
                }
                beanKeys.add(fldName);
                // 别名
                if (fld.hasAliasFields()) {
                    for (WnBeanField fa : fld.getAliasFields()) {
                        String akey = fa.getName();
                        if (isKeyCanOutput(fldName)) {
                            beanKeys.add(akey);
                        }
                    }
                }
            }
            return beanKeys;
        }

        // 简易映射
        if (null != fields && !fields.isEmpty()) {
            List<String> beanKeys = new ArrayList<>(fields.size());
            for (SheetField fld : fields) {
                String key = fld.getKey();
                if (isKeyCanOutput(key)) {
                    beanKeys.add(key);
                }
            }
            return beanKeys;
        }

        // 啥都木有咯
        return null;
    }

    private boolean isKeyCanOutput(String key) {
        return null == this.keys || this.keys.match(key);
    }

    private boolean __is_match(NutBean obj) {
        // 省略
        if (null != omit && omit.match(obj)) {
            return false;
        }
        // 匹配
        if (null != pick && !pick.match(obj)) {
            return false;
        }
        // 匹配的
        return true;
    }

    public List<NutBean> doMapping(List<? extends NutBean> inputList) {
        List<NutBean> outputList = new ArrayList<>(inputList.size());

        try {
            // 指定了映射: WnBeanMapping
            if (null != this.beanMapping) {
                // 设置键过滤器
                this.beanMapping.setPickKeys(keys);
                this.beanMapping.setPickNames(names);

                // 转换并记入输出列表
                int index = 0;
                for (NutBean obj : inputList) {
                    NutBean map = this.beanMapping.translate(obj, true);
                    // 尝试过滤并记入结果
                    index = try_add_to_output_list(outputList, index, map);
                }
            }
            // 简易映射
            else if (null != fields && !fields.isEmpty()) {
                int index = 0;
                for (NutBean obj : inputList) {
                    // 执行映射
                    NutMap map = new NutMap();
                    for (SheetField sf : fields) {
                        String key = sf.getKey();
                        // 不能输出的话就别转换了
                        if (!this.isKeyCanOutput(key)) {
                            continue;
                        }
                        Object val = sf.getValue(obj);
                        map.put(key, val);
                    }
                    // 尝试过滤并记入结果
                    index = try_add_to_output_list(outputList, index, map);
                }
            }
            // 没有指定映射的话，所有的字段都变字符串
            else {
                int index = 0;
                for (NutBean obj : inputList) {
                    // 执行映射
                    NutBean map = new NutMap();
                    for (Map.Entry<String, Object> en : obj.entrySet()) {
                        String key = en.getKey();
                        Object val = en.getValue();
                        if (null != val) {
                            Mirror<?> mi = Mirror.me(val);
                            // 原生的话，保留
                            // 其他的变字符串
                            if (!mi.isSimple()) {
                                val = Castors.me().castToString(val);
                            }
                        }
                        map.put(key, val);
                    }
                    // 尝试过滤并记入结果
                    index = try_add_to_output_list(outputList, index, map);
                }
            }
        }
        // 声明了退出
        catch (WnBreakException e) {}

        // 嗯，搞定返回吧
        return outputList;
    }

    private int try_add_to_output_list(List<NutBean> outputList, int index, NutBean map)
            throws WnBreakException {
        if (this.__is_match(map)) {
            index++;
            // 自增计数并查看跳过
            if (skip > 0 && index <= skip) {
                return index;
            }
            // 达到限制
            if (limit > 0 && outputList.size() >= limit) {
                throw new WnBreakException();
            }
            // 加入
            outputList.add(map);
        }
        return index;
    }

    private static final Pattern P_KEY = Regex.getPattern("^([^\\]]+)(\\[(.+)\\])?$");
    private static final Pattern P_KEY_ARRAY = Regex.getPattern("^[$@]n([.](.+))?$");
    private static final Pattern P_KEY_DATE = Regex.getPattern("^[$@]date(%(.+))?$");
    private static final Pattern P_KEY_BOOLEAN = Regex.getPattern("^[$@]boolean(((->)|(<-))(.+)/(.+))?$");
    private static final Pattern P_KEY_MAPPING = Regex.getPattern("^[$@][{]([^}]+)[}]$");
    private static final Pattern P_KEY_INT = Regex.getPattern("^[$@]int(=(.+))?$");
    private static final Pattern P_KEY_LONG = Regex.getPattern("^[$@]long(=(.+))?$");
    private static final Pattern P_KEY_FLOAT = Regex.getPattern("^[$@]float(=(.+))?$");
    private static final Pattern P_KEY_DOUBLE = Regex.getPattern("^[$@]double(=(.+))?$");
    private static final Pattern P_KEY_STR = Regex.getPattern("^[$@]str(=(.+))?$");

    public void parse(String flds) {
        // 空字段
        if (Strings.isBlank(flds)) {
            fields = null;
            return;
        }
        // 按行解析
        String[] lines = Strings.splitIgnoreBlank(flds, "[,\n]");
        fields = new ArrayList<>(lines.length);

        // 循环
        for (String line : lines) {
            String key, title;
            // 首先字符串最后一个冒号用来做标题
            // 防止 `key[$date%yyyy-MM-dd HH:mm:ss]:日期` 这样情况
            int pos = line.lastIndexOf(':');
            int pos2 = line.lastIndexOf(']');
            if (pos > 0 && pos > pos2) {
                key = Strings.trim(line.substring(0, pos));
                title = Strings.trim(line.substring(pos + 1));
            }
            // 否则就是没有标题，只有 key
            else {
                key = Strings.trim(line);
                title = null;
            }

            // 解析 key
            Matcher m = P_KEY.matcher(key);
            if (!m.find()) {
                throw Er.create("e.sheet.invalidMappingKey", line);
            }
            String knm = m.group(1);
            String kconf = m.group(3);

            // 对于 knm 按照 `||` 分隔，以便表示备选顺序
            SheetField sf = new SheetField();
            sf.keys = Strings.splitIgnoreBlank(knm, "[|][|]");
            sf.title = title;
            fields.add(sf);

            // 看看有木有特殊配置
            if (!Strings.isBlank(kconf)) {
                // 是整数吗？
                m = P_KEY_INT.matcher(kconf);
                if (m.find()) {
                    sf.type = SheetFieldType.INT;
                    String arg = m.group(2);
                    if (!Strings.isBlank(arg)) {
                        sf.arg = Integer.parseInt(arg);
                    }
                    continue;
                }
                // 是长整数吗？
                m = P_KEY_LONG.matcher(kconf);
                if (m.find()) {
                    sf.type = SheetFieldType.LONG;
                    String arg = m.group(2);
                    if (!Strings.isBlank(arg)) {
                        sf.arg = Long.parseLong(arg);
                    }
                    continue;
                }
                // 是浮点吗？
                m = P_KEY_FLOAT.matcher(kconf);
                if (m.find()) {
                    sf.type = SheetFieldType.FLOAT;
                    String arg = m.group(2);
                    if (!Strings.isBlank(arg)) {
                        sf.arg = Float.parseFloat(arg);
                    }
                    continue;
                }
                // 是双精度浮点吗？
                m = P_KEY_DOUBLE.matcher(kconf);
                if (m.find()) {
                    sf.type = SheetFieldType.DOUBLE;
                    String arg = m.group(2);
                    if (!Strings.isBlank(arg)) {
                        sf.arg = Double.parseDouble(arg);
                    }
                    continue;
                }
                // 是字符串吗？（要强制转换的）
                m = P_KEY_STR.matcher(kconf);
                if (m.find()) {
                    sf.type = SheetFieldType.STR;
                    sf.arg = m.group(2);
                    continue;
                }
                // 是布尔吗: ^[$@]boolean(((->)|(<-))(.+)/(.+))?$
                m = P_KEY_BOOLEAN.matcher(kconf);
                if (m.find()) {
                    sf.type = SheetFieldType.BOOLEAN;
                    if (!Strings.isBlank(m.group(1))) {
                        String[] ss = new String[3];
                        ss[0] = Strings.sBlank(m.group(3), m.group(4));
                        ss[1] = Strings.sBlank(m.group(5), "Yes");
                        ss[2] = Strings.sBlank(m.group(6), "No");
                        sf.arg = ss;
                    }
                    continue;
                }
                // 是映射吗
                m = P_KEY_MAPPING.matcher(kconf);
                if (m.find()) {
                    sf.type = SheetFieldType.MAPPING;
                    NutMap map = new NutMap();
                    String str = m.group(1);
                    String[] ss = Strings.splitIgnoreBlank(str, ";");
                    for (String s : ss) {
                        Pair<String> p = Pair.create(s);
                        String pv = p.getValue();
                        // 如果是布尔
                        if (pv.matches("^(true|false)$")) {
                            map.put(p.getName(), Boolean.parseBoolean(pv));
                        }
                        // 如果是整数
                        else if (pv.matches("^[0-9]+$")) {
                            map.put(p.getName(), Integer.parseInt(pv));
                        }
                        // 默认就是字符串咯
                        else {
                            map.put(p.getName(), p.getValue());
                        }
                    }
                    sf.arg = map;
                    continue;
                }
                // 是数组吗？
                m = P_KEY_ARRAY.matcher(kconf);
                if (m.find()) {
                    sf.type = SheetFieldType.ARRAY;
                    sf.arg = m.group(2);
                    continue;
                }
                // 是日期吗？
                m = P_KEY_DATE.matcher(kconf);
                if (m.find()) {
                    sf.type = SheetFieldType.DATE;
                    sf.arg = m.group(2);
                    continue;
                }
            }
            // 那就是普通咯
            sf.type = SheetFieldType.NORMAL;
            sf.arg = null;
        }
    }

}
