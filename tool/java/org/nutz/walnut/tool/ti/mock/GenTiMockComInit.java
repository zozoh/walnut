package org.nutz.walnut.tool.ti.mock;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

/**
 * 生成 Ti 的控件测试初始化脚本
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class GenTiMockComInit {

    public static void main(String[] args) {
        String home = "D:/workspace/git/github/titanium/src/test/components";
        File dHome = new File(home);

        String HR = Strings.dup('-', 57);

        File[] dirs = dHome.listFiles();
        for (File dir : dirs) {
            // 不是目录，就无视
            if (!dir.isDirectory())
                continue;
            String dirName = dir.getName();
            // 循环列出其内内容
            for (File f : dir.listFiles()) {
                if (!f.isFile())
                    continue;

                String fnm = f.getName();
                if (!fnm.endsWith(".json") || fnm.endsWith(".data.json"))
                    continue;

                // 总结一下名字
                String name = Files.getMajorName(fnm);
                String[] nms = Strings.splitIgnoreBlank(name, "-");

                // 计算名称
                String onm = Lang.concat(1, nms.length, "-", nms).toString();
                String title = Lang.concat(2, nms.length, "-", nms).toString().toUpperCase();

                // 输出
                System.out.printf("#%s\n", HR);
                System.out.printf("@FILE mock/components/%s/%s.comt '%s'\n", dirName, onm, title);
                System.out.printf("{com:'/rs/ti/test/components/%s/%s.json'}\n", dirName, name);

                // 如果有初始化内容
                File fd = Files.getFile(dir, name + ".data.json");
                if (fd.exists()) {
                    System.out.printf("?COPY> /rs/ti/test/components/%s/%s.data.json\n",
                                      dirName,
                                      name);
                }
            }
        }
    }

}
