package org.nutz.walnut.tool.doc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Files;

public class GenReadMe {

    public static void main(String[] args) {
        String phHome = "D:/workspace/git/github/walnut/doc/markdown";
        if (args.length > 0) {
            phHome = args[0];
        }

        File dHome = new File(phHome);
        File[] fs = dHome.listFiles();
        List<DocItem> list = new ArrayList<>(fs.length);
        for (File f : fs) {
            if (!f.isHidden() && f.isDirectory()) {
                DocItem di = genItem(dHome, f);
                if (null != di)
                    list.add(di);
            }
        }

        // 打印
        StringBuilder sb = new StringBuilder("---\n");
        sb.append("title  : Walnut 文档目录\n");
        sb.append("author : zozohtnt@gmail.com\n");
        sb.append("---\n\n");
        for (DocItem di : list) {
            di.joinString(sb);
        }
        sb.append('\n');
        for (DocItem di : list) {
            di.joinRefers(sb);
        }

        System.out.println(sb);

        // 写入
        File fReadme = Files.getFile(dHome, "README.md");
        if (!fReadme.exists()) {
            fReadme = Files.createFileIfNoExists(fReadme);
        }
        Files.write(fReadme, sb);

    }

    public static DocItem genItem(File dHome, File file) {
        if (file.isDirectory()) {
            DocGroup g = new DocGroup(dHome, file);
            File[] fs = file.listFiles();
            for (File f : fs) {
                if (!f.isHidden()
                    && f.isFile()
                    && "md".equals(Files.getSuffixName(f))
                    && !f.getName().startsWith("_")) {
                    g.addItem(f);
                }
            }
            return g.sortItems();
        }

        if (file.isFile() && "md".equals(Files.getSuffixName(file))) {
            return new DocItem(dHome, file);
        }

        return null;
    }

}
