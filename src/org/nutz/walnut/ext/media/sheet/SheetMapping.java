package org.nutz.walnut.ext.media.sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.meta.Pair;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.api.err.Er;

public class SheetMapping {

    private NutMap filter;

    private NutMap matcher;

    private int limit;

    private int skip;

    private List<SheetField> fields;

    public SheetMapping() {
        this.filter = null;
        this.matcher = null;
        this.limit = 0;
        this.skip = 0;
    }

    public NutMap getFilter() {
        return filter;
    }

    public void setFilter(NutMap filter) {
        this.filter = filter;
    }

    public NutMap getMatcher() {
        return matcher;
    }

    public void setMatcher(NutMap matcher) {
        this.matcher = matcher;
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

    private boolean __is_match(NutMap obj) {
        // 过滤
        if (null != filter && !filter.isEmpty() && filter.match(obj)) {
            return false;
        }
        // 匹配
        if (null != matcher && !matcher.isEmpty() && !matcher.match(obj)) {
            return false;
        }
        // 匹配的
        return true;
    }

    public List<NutMap> doMapping(List<NutMap> inputList) {
        List<NutMap> outputList = new ArrayList<>(inputList.size());

        // 没有指定映射的话，所有的字段都变字符串
        if (null == fields || fields.isEmpty()) {
            int index = 0;
            for (NutMap obj : inputList) {
                // 执行映射
                NutMap map = new NutMap();
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
                // 过滤
                if (this.__is_match(map)) {
                    // 自增计数并查看跳过
                    if (skip > 0 && ++index <= skip) {
                        continue;
                    }
                    // 达到限制
                    if (limit > 0 && outputList.size() >= limit) {
                        break;
                    }
                    // 加入
                    outputList.add(map);
                }
            }
        }
        // 否则按照映射处理
        else {
            int index = 0;
            for (NutMap obj : inputList) {
                // 执行映射
                NutMap map = new NutMap();
                for (SheetField sf : fields) {
                    String key = sf.getKey();
                    Object val = sf.getValue(obj);
                    map.put(key, val);
                }
                // 过滤
                if (this.__is_match(map)) {
                    // 自增计数并查看跳过
                    if (skip > 0 && ++index <= skip) {
                        continue;
                    }
                    // 达到限制
                    if (limit > 0 && outputList.size() >= limit) {
                        break;
                    }
                    // 加入
                    outputList.add(map);
                }
            }
        }

        // 嗯，搞定返回吧
        return outputList;
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
