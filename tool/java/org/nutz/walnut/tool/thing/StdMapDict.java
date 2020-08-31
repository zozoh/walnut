package org.nutz.walnut.tool.thing;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.FileVisitor;
import org.nutz.lang.util.NutMap;

public class StdMapDict {

    public static NutMap reverDict(NutMap map) {
        NutMap revMap = new NutMap();
        for (String key : map.keySet()) {
            String val = map.getString(key);
            revMap.put(val, key);
        }
        return revMap;
    }

    public static NutMap getRevDict(String aph) {
        NutMap map = getDict(aph);
        return reverDict(map);
    }

    public static NutMap getDict(String aph) {
        File dHome = Files.findFile(aph);

        // 寻找一个列表
        List<File> fList = findCandidateFiles(dHome, ".json");

        // 准备一个 Map
        NutMap i18n = new NutMap();

        // 循环处理
        for (File f : fList) {
            NutMap map = Json.fromJsonFile(NutMap.class, f);
            i18n.putAll(map);
        }

        // 搞定
        return i18n;
    }

    public static NutMap getProperties(String aph) {
        File dHome = Files.findFile(aph);

        // 寻找一个列表
        List<File> fList = findCandidateFiles(dHome, ".properties");

        // 准备一个 Map
        NutMap i18n = new NutMap();

        // 循环处理
        for (File f : fList) {
            InputStream ins = Streams.fileIn(f);
            PropertiesProxy pp = new PropertiesProxy(ins);
            i18n.putAll(pp.toMap());
        }

        // 搞定
        return i18n;
    }

    public static List<File> findCandidateFiles(File dHome, String suffix) {
        List<File> fList = new LinkedList<>();
        Disks.visitFile(dHome, new FileVisitor() {
            public void visit(File f) {
                fList.add(f);
            }
        }, new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    // if (f.getName().matches("^(vod|videos)$")) {
                    // return true;
                    // }
                    // return false;
                    return true;
                }

                if (f.isFile())
                    return f.getName().endsWith(suffix);
                return false;
            }
        });
        return fList;
    }

}
