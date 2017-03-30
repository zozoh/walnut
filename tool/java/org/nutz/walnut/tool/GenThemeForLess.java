/**
 * 
 */
package org.nutz.walnut.tool;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.FileVisitor;

/**
 * @author pw
 *
 */
public class GenThemeForLess {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // 得到工作目录
        String dPath = args[0];
        String themeName = args[1];
        File dHome = Files.findFile(dPath);

        // 找到任何 less 文件
        Disks.visitFile(dHome, new FileVisitor() {
            @Override
            public void visit(File f) {
                // 如果文件名不为 ui.less, 不以下划线开头，只包括数字和字母的，就搞
                String fnm = Files.getMajorName(f);

                // Match the new less file like : _xx_xx.less
                Matcher m = Pattern.compile("^_([a-z0-9_]+)$").matcher(fnm);
                if (m.find()) {
                    String fnm2 = m.group(1);
                    __gen_theme_less_file(themeName, f.getParent(), fnm2);
                }
                // ignore
                else if (fnm.equals("ui") || fnm.startsWith("_")) {
                    return;
                }
                // Gen for orignal pattern
                else if (fnm.matches("^[a-z0-9_]+$")) {
                    System.out.println(f.getAbsolutePath());
                    genThemeLess(f, themeName);
                }

            }
        }, new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                return f.getName().endsWith(".less");
            }
        });

    }

    public static void genThemeLess(File oldLess, String themeName) {
        String fpPath = oldLess.getParent();
        String fnm = oldLess.getName();
        String fMajor = Files.getMajorName(oldLess);
        // 判断是否有_${fnm}.less
        File newLess = Files.getFile(oldLess.getParentFile(), "_" + fnm);
        if (!newLess.exists() || newLess.length() == 0) {
            // 读取文件内容
            String fcontent = Files.read(oldLess);
            String[] fclines = fcontent.split("\r?\n");
            // 删除 import "../ui.less"
            int pos = 0;
            boolean foundImport = false;
            for (; pos < fclines.length; pos++) {
                String line = fclines[pos];
                if (line.startsWith("@import") && line.contains("/ui")) {
                    foundImport = true;
                    break;
                }
            }
            // 写入_${fnm}.less中
            String newContent = foundImport ? Lang.concat(pos
                                                          + 1,
                                                          fclines.length - pos,
                                                          "\n",
                                                          fclines)
                                                  .toString()
                                            : fcontent;
            Files.write(Files.createFileIfNoExists(newLess), newContent);
        }
        __gen_theme_less_file(themeName, fpPath, fMajor);
    }

    private static void __gen_theme_less_file(String themeName, String fpPath, String fMajor) {
        // 生成${fnm}_${themeName}.less
        File themeLess = Files.createFileIfNoExists2(fpPath
                                                     + "/"
                                                     + fMajor
                                                     + "-"
                                                     + themeName
                                                     + ".less");
        String themeContent = "";
        themeContent += String.format("@import \"%s\";\n", themeName);
        themeContent += String.format("@import \"_%s\";\n", fMajor);
        Files.write(themeLess, themeContent);
    }

}
