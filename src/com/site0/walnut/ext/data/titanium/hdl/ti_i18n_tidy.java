package com.site0.walnut.ext.data.titanium.hdl;

import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.titanium.tidyi18n.WnTidyI18nJsonMap;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class ti_i18n_tidy implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String path = hc.params.val_check(0);
        WnObj dHome = Wn.checkObj(sys, path);

        if (dHome.isFILE()) {
            _tidy_file(sys, dHome);
        }
        // 目录的话，查找一下
        else if (dHome.isDIR()) {
            _tidy_dir(sys, dHome, dHome);
        }
    }

    public static void _tidy_file(WnSystem sys, WnObj f) {
        String json = new WnTidyI18nJsonMap(sys.io, f).doTidy();
        sys.io.writeText(f, json);
    }

    private static void _tidy_dir(WnSystem sys, WnObj dHome, WnObj dir) {
        List<WnObj> files = sys.io.getChildren(dir, null);
        for (WnObj f : files) {
            // 目录递归
            if (f.isDIR()) {
                _tidy_dir(sys, dHome, f);
                continue;
            }
            // 确保是文件
            if (!f.isFILE()) {
                continue;
            }
            // 必须是 ".i18n.json"
            if (!f.name().endsWith(".i18n.json")) {
                continue;
            }
            // 嗯，处理吧
            String rph = Wn.Io.getRelativePath(dHome, f);
            sys.out.printlnf(" - tidy: %s", rph);
            _tidy_file(sys, f);
        }
    }

}
