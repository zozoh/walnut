package org.nutz.walnut.ext.titanium.util;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public class TiComScaning {

    private WnObj oHome;

    private List<TiCom> list;

    private WnIo io;

    public TiComScaning(WnIo io, WnObj oHome) {
        this.io = io;
        this.oHome = oHome;
        this.list = new LinkedList<>();
    }

    public List<TiCom> doScan() {
        // 扫描控件库
        List<WnObj> children = io.getChildren(oHome, null);
        for (WnObj child : children) {
            this.scanDir(child);
        }

        return list;
    }

    private void scanDir(WnObj oDir) {
        // Guard
        if (!oDir.isDIR()) {
            return;
        }

        System.out.printf("scan: %s\n", oDir.path());

        // 有编辑定义
        WnObj _hmaker = io.fetch(oDir, "_hmaker.json");
        if (null != _hmaker) {
            // 也有控件标准定义
            WnObj _com = io.fetch(oDir, "_com.json");
            if (null != _com) {
                NutMap dfn = io.readJson(_com, NutMap.class);
                // 是全局控件
                if (dfn.getBoolean("globally")) {
                    String comName = dfn.getString("name");
                    // 名称合法
                    if (!Strings.isBlank(comName)) {
                        // 嗯，那么读取定义吧
                        TiCom com = io.readJson(_hmaker, TiCom.class);
                        com.setName(comName);

                        // 计算相对路径
                        String rph = Wn.Io.getRelativePath(oHome, oDir);
                        if (rph.endsWith("/")) {
                            rph = rph.substring(0, rph.length() - 1);
                        }
                        com.setPath(rph);

                        // 计入结果
                        list.add(com);
                    }
                }
            }
        }

        // 继续递归
        List<WnObj> children = io.getChildren(oDir, null);
        for (WnObj child : children) {
            this.scanDir(child);
        }
    }

}
