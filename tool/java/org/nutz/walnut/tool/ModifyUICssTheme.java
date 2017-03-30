package org.nutz.walnut.tool;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.FileVisitor;

public class ModifyUICssTheme {

    public static void main(String[] args) {
        // 得到工作目录
        String dPath = args[0];
        File dHome = Files.findFile(dPath);

        // 找到任何 js 文件
        // 找到任何 less 文件
        Disks.visitFile(dHome, new FileVisitor() {
            @Override
            public void visit(File f) {
                System.out.println(Strings.dup('-', 40));
                // 分析每一行
                String str = Files.read(f);
                String[] lines = str.split("\r?\n");
                for (int i = 0; i < lines.length; i++) {
                    // 找到引入 css 的行
                    String line = lines[i];

                    if (Strings.trim(line).startsWith("css")) {

                        String regex = "^( *css *: *[\"'])(theme/)(.+)([.]css[\"'] *, *)$";
                        Matcher m = Pattern.compile(regex).matcher(line);

                        if (m.find()) {
                            // System.out.println(Dumps.matcherFound(m));
                            String line2 = String.format("%s%s-{{theme}}%s",
                                                         m.group(1),
                                                         m.group(3),
                                                         m.group(4));
                            System.out.printf("> %s:\n", Disks.getRelativePath(dHome, f));
                            System.out.printf("%s\n%s\n", line, line2);
                            lines[i] = line2;
                            String str2 = Lang.concat("\n", lines).toString();
                            Files.write(f, str2);
                            return;
                        }
                    }
                }
                System.out.printf("!!! NO-THEME> %s:\n", Disks.getRelativePath(dHome, f));

            }
        }, new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    if (f.getName().equals("i18n"))
                        return false;
                    return true;
                }
                return f.getName().endsWith(".js");
            }
        });
    }

}
