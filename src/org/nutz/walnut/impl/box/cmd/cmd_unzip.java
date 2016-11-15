package org.nutz.walnut.impl.box.cmd;

import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class cmd_unzip extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        // 必须带参数
        if (args.length == 0) {
            throw Err.create("e.cmd.unzip.miss_args");
        }
        // 当前仅支持l选项,即list zip content
        ZParams params = ZParams.parse(args, "l");
        if (params.vals.length == 0) {
            throw Err.create("e.cmd.unzip.miss_zip_path");
        }
        // 获取全路径
        String path = Wn.normalizeFullPath(params.val(0), sys);
        boolean listOnly = params.is("l");
        WnObj obj = sys.io.check(null, path);
        byte[] buf = new byte[8192];
        // 开始遍历zip文件
        try (ZipInputStream zis = new ZipInputStream(sys.io.getInputStream(obj, 0))) {
            ZipEntry en = null;
            while (true) {
                en = zis.getNextEntry();
                if (en == null)
                    break;
                // 输出当前正在处理的zip实体
                sys.out.println(en.getName() + " " + en.getSize());
                if (listOnly) {
                    continue;
                }
                // 文件夹就不管了
                if (en.isDirectory())
                    continue;
                String name = en.getName();
                // 预防/开头的压缩包, 防范之
                if (name.startsWith("/"))
                    name = name.substring(1);
                // 输出文件内容
                WnObj tmp = sys.io.createIfNoExists(this.getCurrentObj(sys), name, WnRace.FILE);
                try (OutputStream out = sys.io.getOutputStream(tmp, 0)) {
                    while (true) {
                        int len = zis.read(buf);
                        if (len < 0)
                            break;
                        if (len > 0) {
                            out.write(buf, 0, len);
                        }
                    }
                }
            }
        }
    }

}
