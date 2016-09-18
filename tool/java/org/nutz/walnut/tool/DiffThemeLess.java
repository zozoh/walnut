package org.nutz.walnut.tool;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * 看看 theme 目录里面的 less 与默认位置的 less 是否有区别
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class DiffThemeLess {

    private static final Log log = Logs.get();

    public static void main(String[] args) {
        new DiffThemeLess("~/workspace/git/github/walnut/ROOT").doDiff();

    }

    private File home;

    private File dirTheme;

    private File dirUI;

    private File dirApp;

    public DiffThemeLess(String homePath) {
        // 得到工程目录
        this.home = Files.findFile(homePath);
        log.infof("workspace: %s", home.getAbsolutePath());

        // 得到 theme 目录
        this.dirTheme = Files.getFile(home, "rs/theme");

        // 得到 ui 目录
        this.dirUI = Files.getFile(home, "rs/core/js/ui");

        // 得到 app 目录
        this.dirApp = Files.getFile(home, "app");
    }

    public void doDiff() {
        // 得到 theme 列表
        File[] dThemes = dirTheme.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return !f.isHidden() && f.isDirectory();
            }
        });
        log.infof("found %d theme", dThemes.length);

        // 循环每个 Theme
        for (int i = 0; i < dThemes.length; i++) {
            __do_theme(i, dThemes[i]);
        }
    }

    private void __do_theme(int index, File dTheme) {
        File[] dirs = dTheme.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return !f.isHidden() && f.isDirectory();
            }
        });
        if (null == dirs)
            return;
        log.info(Strings.dup('-', 60));
        log.infof("@THEME[%d] : %s / %d dirs", index, dTheme.getName(), dirs.length);
        log.info(Strings.dup('-', 60));

        // 处理每个 UI
        for (int i = 0; i < dirs.length; i++) {
            __do_dir_in_theme(i, dirs[i]);
        }
    }

    private void __do_dir_in_theme(int index, File dir) {
        String dirName = dir.getName();

        // 是一个 UI 吗
        File coreDir = Files.getFile(dirUI, dirName);

        // 如果不是，是一个 app 吗？
        if (!coreDir.exists()) {
            coreDir = Files.getFile(dirApp, dirName);
        }

        // 如果还不是，那么警告一下忽略
        if (!coreDir.exists()) {
            log.warnf("dir [%s] neither app or ui!!!", dirName);
            return;
        }

        // 分析目录得到对应的 less 文件列表
        File[] lessFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".less");
            }
        });

        log.infof("%2d. %s : %d less %s => %s",
                  index,
                  dirName,
                  lessFiles.length,
                  lessFiles.length > 1 ? "files" : "file",
                  Disks.getRelativePath(home, coreDir));

        // 处理每个 less 文件
        for (int i = 0; i < lessFiles.length; i++) {
            __do_less(i, coreDir, lessFiles[i]);
        }
    }

    private void __do_less(int index, File coreDir, File lessFile) {
        // 找到对应的 lessFile
        String fnm = lessFile.getName();
        File taLess = Files.getFile(coreDir, fnm);

        // 如果没找到，警告一下
        if (!taLess.exists()) {
            log.warnf("    - !noexists in core: %s", fnm);
            return;
        }

        // 两个文件计算一个下 MD5 比较一下
        String md5Less = Lang.md5(lessFile);
        String md5Ta = Lang.md5(taLess);

        // 如果相等，那么打印成
        if (md5Less.equals(md5Ta)) {
            log.warnf("    - OK: %s", fnm);
        }
        // 否则警告
        else {
            log.warnf("    - !!! MD5 fail: diff %s %s",
                      Disks.getRelativePath(home, taLess),
                      Disks.getRelativePath(home, lessFile));
        }
    }

}
