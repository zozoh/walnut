package org.nutz.walnut.tool;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.FileVisitor;

public class MoveThemeLessToFolder {

    public static void main(String[] args) {
        // 得到工作目录
        String dPath = args[0];
        File dHome = Files.findFile(dPath);

        // 找到任何 less 文件
        Disks.visitFile(dHome, new FileVisitor() {
            @Override
            public void visit(File f) {
                // 已经在 theme 文件内的，不移动
                if (f.getParentFile().getName().equals("theme")) {
                    return;
                }
                // 如果是 theme 文件
                String fnm = f.getName();
                String regex = "^([0-9a-z_]+)-(dark-colorized|light)[.](css|less)";
                Matcher m = Pattern.compile(regex).matcher(fnm);

                // 就移动
                if (m.find()) {
                    String nm = m.group(1);
                    String theme = m.group(2);
                    String suffixName = m.group(3);

                    String rph = Disks.getRelativePath(dHome, f);
                    System.out.printf("%s\n%s -> [theme]\n", Strings.dup('-', 40), rph);

                    // less 文件需要改写一下内容
                    if ("less".equals(suffixName)) {
                        String str = String.format("@import \"%s\";\n@import \"../_%s\";\n",
                                                   theme,
                                                   nm);
                        System.out.print(str);
                        Files.write(f, str);
                    }

                    // 执行移动
                    File fTheme = Files.getFile(f.getParentFile(), "theme");
                    Files.createDirIfNoExists(fTheme);
                    try {
                        Files.move(f, Files.getFile(fTheme, f.getName()));
                    }
                    catch (IOException e) {
                        throw Lang.wrapThrow(e);
                    }
                }

            }
        }, new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                return f.getName().endsWith(".less") || f.getName().endsWith(".css");
            }
        });
    }

}
