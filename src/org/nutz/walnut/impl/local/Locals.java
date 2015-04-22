package org.nutz.walnut.impl.local;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public abstract class Locals {

    public static String key2path(String key) {
        return key.substring(0, 2) + "/" + key.substring(2);
    }

    public static File eval_local_file(File home, String rootPath, WnObj o) {
        String path = o.path();
        if (!path.startsWith(rootPath)) {
            throw Er.create("e.io.tree.local.OutOfMount", o);
        }

        // 得到相对路径
        String rpath = path.substring(rootPath.length());
        if (rpath.startsWith("/"))
            rpath = rpath.substring(1);

        // 得到本地文件
        File f = Files.getFile(home, rpath);
        return f;
    }

    /**
     * 这个函数主要是用来做 JUnit 的时候，满足 zozoh 的强迫症
     */
    public static void ocd_clean_data_dir(File f) {
        if (Wn.WC().getBoolean("wn_delete_empty_local_data_dir")) {
            File p = f.getParentFile();
            if (p.list().length == 0) {
                Files.deleteDir(p);
            }
        }
    }

}
