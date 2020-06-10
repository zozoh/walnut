package org.nutz.walnut.tool.ti;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.FileVisitor;

public class ScssAutoImport {
    public static void main(String[] args) throws IOException {
        String workdir = args[0];
        String comDir = null;
        if (args.length > 1) {
            comDir = args[1];
        }

        String hr = Strings.dup('-', 60);

        File fWorkdir = Files.findFile(workdir);

        // 自动计算应该导入的控件
        List<String> includes = evalIncludes(fWorkdir, comDir);
        String includesText = Strings.join("\r\n", includes);
        System.out.printf("Prepare @import:\n%s\n%s\n", includesText, hr);

        System.out.printf("\nFind insert stub\n%s\n\n", hr);
        // 处理内置主题
        for (String fnm : fWorkdir.list()) {
            File f = Files.getFile(fWorkdir, fnm);
            if (!f.isFile() || f.isHidden()) {
                continue;
            }
            if (appendToThemeFile(includes, f)) {
                System.out.printf(" --> %s ++ %d links\n", fnm, includes.size());
            }
        }

        System.out.printf("\n%s\nAll done\n", hr);
    }

    private static boolean appendToThemeFile(List<String> includes, File fTheme)
            throws IOException {
        // 打开主题文件
        BufferedReader br = Streams.buffr(Streams.fileInr(fTheme));

        // 找到插入点
        List<String> lines = new LinkedList<>();
        String line;
        boolean findStub = false;
        while (null != (line = br.readLine())) {
            String trimed = Strings.trim(line);
            lines.add(line);
            if ("// AUTO-INCLUDE-COM-STUB".equals(trimed)) {
                findStub = true;
                break;
            }
        }
        Streams.safeClose(br);

        // 没有插入点，无视
        if (!findStub)
            return false;

        // 插入
        lines.addAll(includes);

        // 写入
        String themeText = Strings.join("\r\n", lines);
        // System.out.println(themeText);
        Files.write(fTheme, themeText);

        return true;
    }

    private static List<String> evalIncludes(File fWorkdir, String comPath) {
        File fDir = Strings.isBlank(comPath) ? fWorkdir : Files.getFile(fWorkdir, comPath);

        List<String> includes = new LinkedList<>();

        Disks.visitFileWithDir(fDir.getAbsolutePath(), "^_.+\\.scss", true, new FileVisitor() {
            public void visit(File file) {
                if (file.isHidden() || !file.isFile())
                    return;

                String rph = Disks.getRelativePath(fWorkdir, file);
                includes.add("@import \"" + rph + "\";");
            }
        });

        Collections.sort(includes);

        return includes;
    }
}
