package org.nutz.walnut.tool.ti;

import java.io.File;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;

public class CompareI18nFolder {

    private static File dHome;

    public static void main(String[] args) {
        dHome = Files.findFile(args[0]);
        String lang0 = "zh-cn";
        String lang1 = "en-us";

        File dir0 = Files.getFile(dHome, lang0);
        File dir1 = Files.getFile(dHome, lang1);

        // 循环文件
        String[] fnms = dir0.list();

        for (String fnm : fnms) {
            if (!fnm.endsWith(".i18n.json")) {
                continue;
            }
            System.out.printf("@%s\n", fnm);

            File f0 = Files.getFile(dir0, fnm);
            File f1 = Files.getFile(dir1, fnm);

            NutMap map0 = Json.fromJsonFile(NutMap.class, f0);
            NutMap map1 = Json.fromJsonFile(NutMap.class, f1);
            int n = Math.abs(map0.size() - map1.size());
            System.out.printf(" Found >> %d items\n\n", n);

            NutMap all = map0.duplicate();
            all.putAll(map1);

            System.out.printf(" > check %s\n", lang0);
            for (String key : all.keySet()) {
                if (!map0.has(key)) {
                    System.out.printf("  - %s\n", key);
                }
            }
            System.out.println();

            System.out.printf(" > check %s\n", lang1);
            for (String key : all.keySet()) {
                if (!map1.has(key)) {
                    System.out.printf("  - %s\n", key);
                }
            }
            System.out.println();
            System.out.println();
        }

    }

}
