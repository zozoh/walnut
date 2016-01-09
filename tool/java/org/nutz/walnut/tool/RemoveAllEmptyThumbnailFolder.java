package org.nutz.walnut.tool;

import java.io.File;

import org.nutz.lang.Files;

public class RemoveAllEmptyThumbnailFolder {

    public static void main(String[] args) {
     // 在图标目录遍历
        File home = Files.findFile("~/workspace/git/github/walnut/ROOT/etc/thumbnail");
        for (File d : home.listFiles()) {
            // 在图标目录 ...
            if (d.isDirectory()) {
                if(d.listFiles().length==0){
                    System.out.println(d);
                    d.delete();
                }
            }
        }
    }

}
