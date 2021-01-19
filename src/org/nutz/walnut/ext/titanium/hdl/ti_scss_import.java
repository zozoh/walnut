package org.nutz.walnut.ext.titanium.hdl;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wcol;
import org.nutz.walnut.util.Wn;

public class ti_scss_import implements JvmHdl {

    private static final String hr = Strings.dup('-', 60);

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String workdir = hc.params.val_check(0);
        WnObj fWorkdir = Wn.checkObj(sys, workdir);

        // 自动计算应该导入的控件
        List<String> includes = new LinkedList<>();

        for (int i = 1; i < hc.params.vals.length; i++) {
            String comDir = hc.params.val_check(i);
            sys.out.printlnf("Scan imports in : '%s'", comDir);
            joinIncludes(sys.io, fWorkdir, includes, comDir);
        }

        // 确保固定的顺序
        Collections.sort(includes);
        String includesText = Strings.join("\r\n", includes);

        sys.out.printlnf("Prepare @import:\n%s\n%s", includesText, hr);
        sys.out.printlnf("\nFind insert stub\n%s\n", hr);

        // 处理内置主题
        List<WnObj> oThemes = sys.io.getChildren(fWorkdir, "^[^_].+\\.scss$");
        for (WnObj f : oThemes) {
            if (!f.isFILE() || f.isHidden() || !f.isType("scss") || f.name().startsWith("_")) {
                continue;
            }
            List<String> lines = appendToThemeFile(sys.io, f, includes);
            if (!lines.isEmpty()) {
                String fnm = f.name();
                sys.out.printlnf(" --> %s ++ %d links", fnm, includes.size());

                String themeText = Wcol.join(lines, System.lineSeparator());
                sys.io.writeText(f, themeText);
            }
        }

        sys.out.printlnf("\n%s\nAll done", hr);
    }

    private static List<String> appendToThemeFile(WnIo io, WnObj fTheme, List<String> includes)
            throws IOException {
        // 打开主题文件
        String text = io.readText(fTheme);
        String[] ss = text.split("\r?\n");

        // 找到插入点
        List<String> lines = new LinkedList<>();
        boolean findStub = false;
        for (int i = 0; i < ss.length; i++) {
            String line = ss[i];
            String trimed = Strings.trim(line);
            lines.add(line);
            if ("// AUTO-INCLUDE-COM-STUB".equals(trimed)) {
                findStub = true;
                break;
            }
        }

        // 没有插入点，无视
        if (!findStub)
            return new LinkedList<>();

        // 插入
        lines.addAll(includes);

        return lines;
    }

    private static void joinIncludes(WnIo io,
                                     WnObj fWorkdir,
                                     List<String> includes,
                                     String comPath) {
        WnObj fDir = Strings.isBlank(comPath) ? fWorkdir : io.check(fWorkdir, comPath);
        io.walk(fDir, file -> {
            if (file.isHidden()
                || !file.isFILE()
                || !file.name().startsWith("_")
                || !file.isType("scss")) {
                return;
            }
            String rph = Wn.Io.getRelativePath(fWorkdir, file);
            includes.add("@import \"" + rph + "\";");
        }, WalkMode.LEAF_ONLY);
    }

}
