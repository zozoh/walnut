package org.nutz.walnut.tool.ti;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;

public class TidyI18nJsonMap {

    private File i18nFile;

    public TidyI18nJsonMap(File f) {
        this.i18nFile = f;
    }

    private NutMap tidyMap(NutMap map) {
        // 排序
        List<String> keys = new ArrayList<>(map.size());
        keys.addAll(map.keySet());
        Collections.sort(keys);

        // 输出
        NutMap out = new NutMap();
        for (String key : keys) {
            Object val = map.get(key);
            // if (val instanceof Map) {
            // NutMap m2 = NutMap.WRAP((Map<String, Object>) val);
            // val = tidyMap(m2);
            // }
            out.put(key, val);
        }

        // 返回
        return out;
    }

    public String doTidy() {
        // 解析
        NutMap map = Json.fromJsonFile(NutMap.class, i18nFile);

        // 排序
        NutMap out = tidyMap(map);

        // 返回
        JsonFormat jfmt = JsonFormat.nice();
        jfmt.setQuoteName(true);
        jfmt.setIndentBy("  ");
        return Json.toJson(out, jfmt);
    }

    public static void _tidy_file(File f) {
        System.out.printf(" - tidy: %s\n", f.getName());
        String json = new TidyI18nJsonMap(f).doTidy();
        Files.write(f, json);
    }

    public static void main(String[] args) {
        String path = "D:/workspace/git/github/titanium/src/i18n/zh-cn/";

        File fOrDir = Files.findFile(path);

        if (fOrDir.isFile()) {
            _tidy_file(fOrDir);
        }
        // 目录的话，查找一下
        else if (fOrDir.isDirectory()) {
            File[] files = fOrDir.listFiles();
            for (File f : files) {
                // 只处理文件
                if (!f.isFile()) {
                    continue;
                }
                // 必须是 ".i18n.json"
                if (!f.getName().endsWith(".i18n.json")) {
                    continue;
                }
                // 嗯，处理吧
                _tidy_file(f);
            }
        }
    }

}
