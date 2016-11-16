package org.nutz.walnut.tool;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.FileVisitor;

public class CompileLess {

    public static void main(String[] args) throws IOException {

        // 得到项目的主目录
        String homePath = "~/workspace/git/github/walnut";
        if (args.length > 0) {
            homePath = args[0];
        }
        File dHome = Files.findFile(homePath);

        // System.out.println(Lang.execOutput("env", Encoding.CHARSET_UTF8));

        Stopwatch sw = Stopwatch.begin();
        int[] n_ok = new int[1];
        int[] n_fail = new int[1];
        __scan_less(dHome, "ROOT/app", n_ok, n_fail);
        __scan_less(dHome, "ROOT/rs/core/js/jquery-plugin", n_ok, n_fail);
        __scan_less(dHome, "ROOT/rs/core/js/ui", n_ok, n_fail);
        __scan_less(dHome, "ROOT/rs/ext", n_ok, n_fail);
        __scan_less(dHome, "ROOT/rs/theme", n_ok, n_fail);
        // __scan_less(dHome, "ROOT/rs/core/js/ui/list", n_ok, n_fail);
        sw.stop();
        System.out.println("--------------------------------------");
        System.out.printf("Done %d less fiels (OK:%d, Fail:%d) in %sms\n",
                          n_ok[0] + n_fail[0],
                          n_ok[0],
                          n_fail[0],
                          sw.getDuration());

    }

    private static void __scan_less(File dHome,
                                    String scanPath,
                                    final int[] n_ok,
                                    final int[] n_fail) {
        // 遍历目录里面的 less 文件
        Disks.visitFile(Files.getFile(dHome, scanPath), new FileVisitor() {
            public void visit(File f) {
                String rph = Disks.getRelativePath(dHome, f);
                // System.out.println(rph);

                String aph = f.getAbsolutePath();
                String cmd = String.format("lessc %s %s",
                                           aph,
                                           Files.renamePath(aph, Files.getMajorName(f) + ".css"));
                // System.out.println(cmd);
                try {
                    StringBuilder out = new StringBuilder();
                    StringBuilder err = new StringBuilder();
                    Lang.exec(cmd, out, err);

                    // String err = Lang.execOutput(cmd).toString();

                    // 成功
                    if (Strings.isEmpty(err)) {
                        System.out.printf("%3d. OK: %s -> css\n", n_ok[0] + n_fail[0], rph);
                        n_ok[0]++;
                    }
                    // 失败
                    else {
                        System.err.printf("%3d. !!: %s\n%s", n_ok[0] + n_fail[0], rph, err);
                        n_fail[0]++;
                    }

                }
                catch (IOException e) {
                    throw Lang.wrapThrow(e);
                }
            }
        }, new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                return f.getName().endsWith(".less");
            }
        });
    }

}
