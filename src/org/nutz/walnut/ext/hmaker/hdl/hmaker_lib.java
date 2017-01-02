package org.nutz.walnut.ext.hmaker.hdl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Disks;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("ocqn")
public class hmaker_lib implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到站点主目录
        WnObj oSiteHome = Hms.checkSiteHome(sys, hc.params.val_check(0));
        // ................................................
        // 如果是写入的话，会自动创建库目录
        if (hc.params.has("write")) {
            __do_write(sys, hc, oSiteHome);
            return;
        }
        // ................................................
        // 得到库的主目录
        WnObj oLibHome = sys.io.fetch(oSiteHome, "lib");
        // ................................................
        // 读取某个库文件
        if (hc.params.has("read")) {
            __do_read(sys, hc, oLibHome);
            return;
        }
        // ................................................
        // 总之处理某个库文件
        WnObj oLib = null;
        // ................................................
        // 删除某个库文件
        if (hc.params.has("del")) {
            if (null != oLibHome) {
                oLib = sys.io.check(oLibHome, hc.params.get("del"));
                sys.io.delete(oLib);
            }
        }
        // ................................................
        // 输出某个库文件元数据
        else if (hc.params.has("get")) {
            if (null != oLibHome) {
                oLib = sys.io.check(oLibHome, hc.params.get("get"));
            }
            hc.params.setv("o", true);
        }
        // ................................................
        // 默认列库名
        else {
            __do_list(sys, hc, oLibHome);
            return;
        }

        // 是否输出这个库文件
        if (hc.params.is("o")) {
            sys.out.println(Json.toJson(oLib, hc.jfmt));
        }
    }

    private void __do_read(WnSystem sys, JvmHdlContext hc, WnObj oLibHome) {
        if (null != oLibHome) {
            WnObj oLib = sys.io.check(oLibHome, hc.params.get("read"));
            String content = sys.io.readText(oLib);
            sys.out.print(content);
        } else {
            throw Er.create("e.cmd.hmaker.lib.noLibHome");
        }
    }

    private void __do_list(WnSystem sys, JvmHdlContext hc, WnObj oLibHome) {
        // 先读取所有的库文件
        List<WnObj> oLibs = new LinkedList<>();
        if (null != oLibHome) {
            sys.io.walk(oLibHome, new Callback<WnObj>() {
                public void invoke(WnObj oLib) {
                    oLibs.add(oLib);
                }
            }, WalkMode.LEAF_ONLY);
        }

        // 准备输出
        String listMode = hc.params.get("list");

        // JSON 对象列表
        if ("obj".equals(listMode)) {
            sys.out.println(Json.toJson(oLibs, hc.jfmt));
        }
        // 名称列表
        else {
            // 首先计算名称
            List<String> nms = new ArrayList<>(oLibs.size());
            if (oLibs.size() > 0) {
                String libHomePath = oLibHome.path();
                for (WnObj oLib : oLibs) {
                    String rph = Disks.getRelativePath(libHomePath, oLib.path());
                    nms.add(rph);
                }
            }
            // 仅仅是对象名，一行一个
            if ("name".equals(listMode)) {
                sys.out.println(Lang.concat("\n", nms));
            }
            // 默认是一个 JSON 数据组，元素是库文件名
            else {
                sys.out.println(Json.toJson(nms));
            }
        }
    }

    private void __do_write(WnSystem sys, JvmHdlContext hc, WnObj oSiteHome) {
        WnObj oLib;
        String libName = hc.params.get("write");
        oLib = sys.io.createIfNoExists(oSiteHome, "lib/" + libName, WnRace.FILE);
        // 得到内容
        String content = hc.params.get("content");
        // 从标准输入得到内容
        if ("true".equals(content) || Strings.isBlank(content)) {
            content = null;
        }
        // 从文件得到内容
        if (null == content && hc.params.has("file")) {
            WnObj o = Wn.checkObj(sys, hc.params.get("file"));
            content = sys.io.readText(o);
        }
        // 默认从标准输入读取
        if (null == content) {
            content = sys.in.readAll();
        }
        // 写入内容
        sys.io.writeText(oLib, content);

        // 是否输出?
        if (hc.params.is("o")) {
            sys.out.println(Json.toJson(oLib, hc.jfmt));
        }
    }

}
