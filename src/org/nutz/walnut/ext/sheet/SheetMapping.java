package org.nutz.walnut.ext.sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.api.err.Er;

public class SheetMapping {

    private List<SheetField> fields;

    private static final Pattern P_KEY = Regex.getPattern("^([^\\]]+)(\\[(.+)\\])?$");
    private static final Pattern P_KEY_ARRAY = Regex.getPattern("^[$@]n([.](.+))?$");
    private static final Pattern P_KEY_DATE = Regex.getPattern("^[$@]date(%(.+))?$");

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

    public List<NutMap> doMapping(List<NutMap> inputList) {
        List<NutMap> outputList = new ArrayList<>(inputList.size());

        // 没有指定映射的话，所有的字段都变字符串
        if (null == fields || fields.isEmpty()) {
            for (NutMap obj : inputList) {
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
                outputList.add(map);
            }
        }
        // 否则按照映射处理
        else {
            for (NutMap obj : inputList) {
                NutMap map = new NutMap();
                for (SheetField sf : fields) {
                    String key = sf.getKey();
                    Object val = sf.getValue(obj);
                    map.put(key, val);
                }
                outputList.add(map);
            }
        }

        // 嗯，搞定返回吧
        return outputList;
    }

}
