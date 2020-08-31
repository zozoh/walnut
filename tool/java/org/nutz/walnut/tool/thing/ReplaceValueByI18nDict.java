package org.nutz.walnut.tool.thing;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;

public class ReplaceValueByI18nDict {

    private static File dHome;

    private static String HR = Strings.dup('-', 60);

    private static JsonFormat jfmt = JsonFormat.nice();

    private static NutMap stdI18n;

    public static void main(String[] args) {
        // 收集标准字典
        dHome = Files.findFile(args[0]);
        stdI18n = StdMapDict.getRevDict(args[1]);
        NutMap ppMap = StdMapDict.getRevDict(args[2]);
        stdI18n.putAll(ppMap);

        // 寻找一个列表
        List<File> fList = StdMapDict.findCandidateFiles(dHome, ".json");
        System.out.printf("Found %d files\n", fList.size());
        System.out.println(HR);

        // 循环处理
        int i = 0;
        for (File f : fList) {
            joinI18nByFile(f, i++);
        }
    }

    private static void joinI18nByFile(File f, int index) {
        String rph = Disks.getRelativePath(dHome, f);
        String json = Strings.trim(Files.read(f));
        if (Strings.isBlank(json)) {
            System.out.printf("! %d). skip: %s\n", index, rph);
            return;
        }
        Object obj = Json.fromJson(json);

        System.out.println(HR);
        System.out.printf("+ %d). JOIN: %s\n", index, rph);
        System.out.println(HR);
        joinI18nByAny(obj);
        json = Json.toJson(obj, jfmt);
        // System.out.println(json);

        Files.write(f, json);
    }

    @SuppressWarnings("unchecked")
    private static void joinI18nByAny(Object obj) {
        if (null == obj)
            return;

        if (obj instanceof Map<?, ?>) {
            NutMap map = NutMap.WRAP((Map<String, Object>) obj);
            joinI18nByMap(map);
        }
        // 集合
        else if (obj instanceof Collection<?>) {
            joinI18nByCollection((Collection<?>) obj);
        }
    }

    private static void joinI18nByCollection(Collection<?> coll) {
        for (Object obj : coll) {
            joinI18nByAny(obj);
        }
    }

    private static void joinI18nByMap(NutMap map) {
        for (Map.Entry<String, Object> en : map.entrySet()) {
            Object val = en.getValue();

            // 字符串
            if (val instanceof String) {
                String str = val.toString();
                // 查字典
                String s2 = stdI18n.getString(str);
                if (!Strings.isBlank(s2)) {
                    en.setValue("i18n:" + s2);
                }
            }
            // 其他
            else {
                joinI18nByAny(val);
            }
        }
    }

}
