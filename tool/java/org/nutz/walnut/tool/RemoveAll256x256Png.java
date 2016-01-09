package org.nutz.walnut.tool;

import java.io.File;

import org.nutz.lang.Files;

public class RemoveAll256x256Png {

    public static void main(String[] args) {
        // 在图标目录遍历
        File home = Files.findFile("~/workspace/git/github/walnut/ROOT/etc/thumbnail");
        for (File d : home.listFiles()) {
            // 在图标目录 ...
            if (d.isDirectory()) {
                // 找文件
                File png = Files.getFile(d, "256x256.png");
                // 如果存在 ..
                if (png.exists()) {
                    System.out.println(png);
                    //Files.deleteFile(png);
                }
            }
        }
    }

}
