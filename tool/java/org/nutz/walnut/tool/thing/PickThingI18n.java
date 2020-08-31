package org.nutz.walnut.tool.thing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.lang.meta.Pair;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;

public class PickThingI18n {

    private static final Log log = Logs.get();

    private static File dHome;

    private static String HR = Strings.dup('-', 60);

    private static JsonFormat jfmt = JsonFormat.nice();

    private static NutMap stdI18n;

    public static void main(String[] args) {
        dHome = Files.findFile(args[0]);
        stdI18n = StdMapDict.getRevDict(args[1]);
        NutMap ppMap = StdMapDict.getProperties(args[2]);
        stdI18n.putAll(StdMapDict.reverDict(ppMap));

        // 寻找一个列表
        List<File> fList = StdMapDict.findCandidateFiles(dHome, ".json");
        log.infof("Found %d files", fList.size());

        // 准备一个 Map
        NutMap i18n = new NutMap();

        // 循环处理
        int i = 0;
        for (File f : fList) {
            joinI18nByFile(i18n, f, i++);
        }

        // 打印
        // JsonFormat jfmt = JsonFormat.nice();
        // log.info(Json.toJson(i18n, jfmt));
        // log.infof("#%s", HR);
        // log.infof("# %d I18n Map", i18n.size());
        // log.infof("#");
        // printI18nMap(i18n);

        // 生成明值对列表，并根据值排序，才能知道那些重了
        List<Pair<String>> pairs = new ArrayList<>(i18n.size());
        for (String key : i18n.keySet()) {
            String val = i18n.getString(key);
            Pair<String> pa = new Pair<>(key, val);
            pairs.add(pa);
        }

        // 按照值来排序
        pairs.sort(new Comparator<Pair<String>>() {
            public int compare(Pair<String> p1, Pair<String> p2) {
                return p1.getValue().compareTo(p2.getValue());
            }
        });

        // 打印出属性列表来
        log.infof("#%s", HR);
        log.infof("#");
        log.infof("# Properties");
        log.infof("#");
        log.infof("#%s", HR);
        for (Pair<String> pa : pairs) {
            System.out.printf("%s=%s\n", pa.getValue(), pa.getName());
        }

        // 分析一下字典
        // evalDictMap(i18n);

    }

    static void evalDictMap(NutMap i18n) {
        // 归纳字典
        NutMap dict = new NutMap();
        for (String key : i18n.keySet()) {
            String msg = dict.getString(key);
            List<String> list = i18n.getList(key, String.class);
            for (String li : list) {
                // 第一个
                if (Strings.isBlank(msg)) {
                    msg = li;
                }
                // 短的不要
                else if (msg.length() > li.length()) {
                    continue;
                }
                // 替换
                else {
                    msg = li;
                }
                // 记入字段
                dict.put(key, msg);
            }
        }

        log.infof("#%s", HR);
        log.infof("# %d Dict Size", dict.size());
        log.infof("#");
        log.info(Json.toJson(dict, jfmt));

        NutMap msgMap = new NutMap();
        for (String key : dict.keySet()) {
            String val = dict.getString(key);
            msgMap.put(val, key);
        }

        log.infof("#%s", HR);
        log.infof("# %d Message Size", msgMap.size());
        log.infof("#");
        log.info(Json.toJson(msgMap, jfmt));

        // 看看那些键重复了
        for (String key : msgMap.keySet()) {
            String val = msgMap.getString(key);
            dict.remove(val);
        }

        log.infof("#%s", HR);
        log.infof("# %d Remain Keys", dict.size());
        log.infof("#");
        log.info(Json.toJson(dict, jfmt));
    }

    private static void joinI18nByFile(NutMap i18n, File f, int index) {
        String rph = Disks.getRelativePath(dHome, f);
        String json = Strings.trim(Files.read(f));
        if (Strings.isBlank(json)) {
            // log.infof("! %d). skip: %s", index, rph);
            return;
        }
        Object obj = Json.fromJson(json);

        // log.infof("+ %d). JOIN: %s", index, rph);

        String[] path = Strings.splitIgnoreBlank(rph, "/");
        ArrayList<String> plist = new ArrayList<>();
        plist.add("oc");
        plist.add(path[0]);
        plist.add(path[1]);
        joinI18nByAny(i18n, obj, plist);
    }

    private static boolean isNotAscii(String str) {
        for (int i = 0; i < str.length(); i++) {
            int c = (int) str.charAt(i);
            if (c > 128) {
                return true;
            }
        }
        return false;
    }

    private static String _eval_mapKey(NutMap map) {
        // 字段
        if (map.has("title") && map.has("name")) {
            return map.getString("name");
        }

        // 选项
        if (map.has("text") && map.has("value")) {
            return map.getString("value");
        }

        return null;
    }

    private static void joinI18nByMap(NutMap i18n, NutMap map, ArrayList<String> plist) {
        String mkey = _eval_mapKey(map);
        if (null != mkey) {
            plist.add(mkey);
        }

        for (String key : map.keySet()) {
            Object val = map.get(key);
            String pkey = _eval_pkey(key);

            if (null != pkey)
                plist.add(pkey);
            // 字符串
            if (val instanceof String) {
                String str = val.toString();
                if (isNotAscii(str)) {
                    // 已经有了标准字段
                    String stdKey = stdI18n.getString(str);
                    if (null != stdKey) {
                        i18n.put(str, stdKey);
                    }
                    // 那么自己搞一个字段
                    else {
                        String keyPath = Strings.join("-", plist);
                        i18n.addv2(str, keyPath);
                    }
                }
            }
            // 其他
            else {
                joinI18nByAny(i18n, val, plist);
            }

            if (null != pkey)
                plist.remove(plist.size() - 1);
        }

        if (null != mkey) {
            plist.remove(plist.size() - 1);
        }
    }

    private static String _eval_pkey(String key) {
        String regex = "^(comConf|fields|form";
        regex += "|text|filter|sorter|list";
        regex += "|desktop|blocks|creator";
        regex += "|title|meta|options)$";
        if (key.matches(regex))
            return null;

        return key;
    }

    private static void joinI18nByCollection(NutMap i18n,
                                             Collection<?> coll,
                                             ArrayList<String> plist) {
        for (Object obj : coll) {
            joinI18nByAny(i18n, obj, plist);
        }
    }

    @SuppressWarnings("unchecked")
    private static void joinI18nByAny(NutMap i18n, Object obj, ArrayList<String> plist) {
        if (null == obj)
            return;

        if (obj instanceof Map<?, ?>) {
            NutMap map = NutMap.WRAP((Map<String, Object>) obj);
            joinI18nByMap(i18n, map, plist);
        }
        // 集合
        else if (obj instanceof Collection<?>) {
            joinI18nByCollection(i18n, (Collection<?>) obj, plist);
        }
    }

}
