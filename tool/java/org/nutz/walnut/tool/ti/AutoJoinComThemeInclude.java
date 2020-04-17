package org.nutz.walnut.tool.ti;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.walnut.util.Wn;

public class AutoJoinComThemeInclude {
    public static void main(String[] args) throws IOException {
        String workSpace = "D:/workspace/git/github";
        String themeHome = Wn.appendPath(workSpace, "/titanium/src/theme");
        File dThemeHome = Files.findFile(themeHome);
        File dComHome = Files.getFile(dThemeHome, "com");

        // 自动计算应该导入的控件
        List<String> includes = evalIncludes(dComHome);
        // String includesText = Strings.join("\r\n", includes);
        // System.out.println(includesText);

        // 处理内置主题
        String[] themes = Lang.array("_all_com", "light", "dark");
        for (String theme : themes) {
            File fTheme = Files.getFile(dThemeHome, theme + ".scss");
            appendToThemeFile(includes, fTheme);
            System.out.printf("done for %s\n", theme);
        }
    }

    private static void appendToThemeFile(List<String> includes, File fTheme) throws IOException {
        BufferedReader br = Streams.buffr(Streams.fileInr(fTheme));
        List<String> lines = new LinkedList<>();
        String line;
        while (null != (line = br.readLine())) {
            String trimed = Strings.trim(line);
            lines.add(line);
            if ("// AUTO-INCLUDE-COM-STUB".equals(trimed)) {
                break;
            }
        }
        Streams.safeClose(br);
        lines.addAll(includes);
        String themeText = Strings.join("\r\n", lines);
        // System.out.println(themeText);
        Files.write(fTheme, themeText);
    }

    private static List<String> evalIncludes(File dComHome) {
        List<String> includes = new LinkedList<>();
        for (File f : dComHome.listFiles()) {
            if (f.isHidden()) {
                continue;
            }
            String sfx = Files.getSuffixName(f);
            if (null != sfx && sfx.equals("scss")) {
                includes.add("@import \"com/" + f.getName() + "\";");
            }
        }
        return includes;
    }
}
