package org.nutz.walnut.impl.box.cmd;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class cmd_unzip extends JvmExecutor {

    private static List<String> hiddenFilePrefixs = new ArrayList<>();

    static {
        hiddenFilePrefixs.add(".");
        hiddenFilePrefixs.add("__MACOSX");
    }

    private boolean isHidden(String nm) {
        for (int i = 0; i < hiddenFilePrefixs.size(); i++) {
            if (nm.startsWith(hiddenFilePrefixs.get(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, "flh");

        // 必须带参数
        if (args.length == 0) {
            throw Err.create("e.cmd.unzip.miss_args");
        }

        byte[] buf = new byte[8192];
        boolean listOnly = params.is("l");
        boolean forceWrite = params.is("f");
        boolean hiddenWrite = params.is("h");
        String matchStr = params.get("m");
        WnObj zipObj = sys.io.check(null, Wn.normalizeFullPath(params.val(0), sys));
        WnObj destDirObj = params.vals.length > 1 ? sys.io.check(null,
                                                                 Wn.normalizeFullPath(params.val(1),
                                                                                      sys))
                                                  : sys.getCurrentObj();
        if (!destDirObj.isDIR()) {
            throw Err.create("e.cmd.unzip.dest_obj.not_dir");
        }

        // 开始遍历zip文件
        try (ZipInputStream zis = new ZipInputStream(sys.io.getInputStream(zipObj, 0))) {
            ZipEntry en = null;
            while (true) {
                en = zis.getNextEntry();
                if (en == null)
                    break;
                // 输出当前正在处理的zip实体
                sys.out.println(en.getName());
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
                // 隐藏文件
                if (isHidden(name) && !hiddenWrite) {
                    continue;
                }
                // 过滤器
                if (!Strings.isBlank(matchStr)) {
                    Pattern pattern = Pattern.compile(matchStr);
                    Matcher matcher = pattern.matcher(name);
                    if (!matcher.find()) {
                        continue;
                    }
                }
                // 输出文件内容
                WnObj tmp = sys.io.createIfNoExists(destDirObj, name, WnRace.FILE);
                if (tmp.size() > 0 && !forceWrite) {
                    continue;
                }
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
