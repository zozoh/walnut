package org.nutz.walnut.ext.app.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class app_rolec extends app_initold {

    @Override
    protected void __exec_init(WnSystem sys, JvmHdlContext hc) {
        // 得到关键目录
        String ph_tmpl = hc.params.val_check(0);
        //String ph_dest = Strings.sBlank(hc.params.val(1), "~");
        
        WnObj oTmpl = Wn.checkObj(sys, ph_tmpl);
        //WnObj oDest = Wn.checkObj(sys, ph_dest);
        
        // 得到上下文
        NutMap c;
        String json = hc.params.get("c");
        // 从 pipe 里读
        if (null == json || "true".equals(json)) {
            json = sys.in.readAll();
        }
        // 直接格式化
        if (!Strings.isBlank(json)) {
            c = Lang.map(json);
        }
        // 就来一个空的吧
        else {
            c = new NutMap();
        }
        
        WnObj oRoles = sys.io.check(oTmpl, "_roles");
        String roles = sys.io.readText(oRoles);
        WnObj wobj = null;
        for (String role : Strings.splitIgnoreBlank(roles, "\n")) {
            if (role.startsWith("#"))
                continue;
            // 格式如下
            // 操作符 路径 值(可选)
            // 存在文件,必须是个文件
            // F /xxx/xxx/xxx 
            // 存在目录
            // D /xxx/yyy/xxx
            // 读写权限
            // R /xxx/zzz 755
            role = Tmpl.exec(role, c, true);
            String[] tmp = Strings.splitIgnoreBlank(role.replace('\t', ' '), " ");
            if (tmp.length < 2)
                continue;
            String msg = "";
            switch (tmp[0]) {
            case "F":
                wobj = sys.io.fetch(null, Wn.normalizeFullPath(tmp[1], sys));
                if (wobj == null) {
                    msg = "FAIL : " + role + " - not exists";
                }
                else if (!wobj.isFILE()) {
                    msg = "FAIL : " + role + " - not a file";
                }
                else {
                    msg = "PASS : " + role;
                }
                break;
            case "D":
                wobj = sys.io.fetch(null, Wn.normalizeFullPath(tmp[1], sys));
                if (wobj == null) {
                    msg = "FAIL : " + role + " - not exists";
                }
                else if (!wobj.isDIR()) {
                    msg = "FAIL : " + role + " - not a dir";
                }
                else {
                    msg = "PASS : " + role;
                }
                break;
            case "R":
                wobj = sys.io.fetch(null, Wn.normalizeFullPath(tmp[1], sys));
                if (wobj == null) {
                    msg = "FAIL : " + role + " - not exists";
                }
                else {
                    int mark = wobj.mode();
                    int mark_expect = Integer.parseInt(tmp[2], 8);
                    if (mark != mark_expect) {
                        msg = "FAIL : " + role + " - got " + Integer.toString(mark, 8);
                    }
                    else {
                        msg = "PASS : " + role;
                    }
                }
                break;
            default:
                msg = "FAIL : " + role + " - unkown check pattern";
                break;
            }
            sys.out.println(msg);
        }
    }
}
