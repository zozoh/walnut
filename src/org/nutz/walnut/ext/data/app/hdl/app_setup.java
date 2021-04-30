package org.nutz.walnut.ext.data.app.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.app.WnApps;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

@JvmHdlParamArgs("cqn")
public class app_setup implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 获取对象
        WnObj o;
        if (hc.params.vals.length > 0) {
            o = Wn.checkObj(sys, hc.params.vals[0]);
        }
        // 默认获取当前路径
        else {
            o = sys.getCurrentObj();
        }

        // 得到所有的 UI 主目录
        List<WnObj> oUIHomes = WnApps.getUIHomes(sys);

        // 根据对象类型获取菜单
        WnObj oFType = null;
        for (WnObj oUiHome : oUIHomes) {
            // 指定的类型
            if (hc.params.has("tp")) {
                String tp = hc.params.get("tp");
                oFType = sys.io.fetch(oUiHome, "ftypes/" + tp + ".js");
            }
            // 根据对象查找
            else {
                oFType = __find_ftype(sys, o, oUiHome);
            }

            // 找到咯
            if (null != oFType)
                break;
        }

        // 还是没有？ 那么就返回空吧
        if (null == oFType) {
            sys.out.println("{actions:[]}");
            return;
        }

        // 要输出的对象，进行整理
        NutMap map = sys.io.readJson(oFType, NutMap.class);

        // 分析动作文件，根据动作内容和权限，过滤菜单项
        List<String> list = __filter_actions(sys, o, map.getList("actions", String.class));
        map.put("actions", list);

        // 输出
        sys.out.println(Json.toJson(map, hc.jfmt));
    }

    /**
     * <pre>
     *  模式     权限     动作 
     * "[@Ee] : [rwx]|[ADMIN(root);MEMBER(root)] : new"
     * </pre>
     */
    private List<String> __filter_actions(WnSystem sys, WnObj o, List<String> actions) {
        List<String> list = new ArrayList<String>(actions.size());

        // 进入内核态，因为可能需要访问用户权限
        sys.nosecurity(new Atom() {
            public void run() {
                WnContext wc = Wn.WC();
                boolean lastIsGroup = false;
                for (String astr : actions) {
                    String str = Strings.trim(astr);
                    // 如果当前是个组，连续多个组会保持一份
                    if ("~".equals(str)) {
                        if (lastIsGroup)
                            continue;
                        lastIsGroup = true;
                        list.add(str);
                        continue;
                    }
                    String[] ss = str.split(":");
                    // 菜单项错误
                    if (ss.length < 2)
                        continue;
                    // 需要验证权限
                    String pvg = ss[1];
                    if (pvg.length() > 0) {
                        // 权限是 rwx 模式
                        if (pvg.matches("^[rwx]+$")) {
                            if (!__is_has_right_to_access(o, wc, pvg))
                                continue;
                        }
                        // 那么就应该是 ADMIN(root);MEMBER(root) 模式
                        else if (!__is_matched_role_of_group(sys, pvg)) {
                            continue;
                        }
                    }
                    // 保存到结果列表
                    list.add(str);

                    // 那么标记一下组
                    lastIsGroup = false;
                }
            }
        });

        return list;
    }

    private static final Pattern _P = Pattern.compile("^(ADMIN|MEMBER)\\((.+)\\)$");

    // ADMIN(root,op);MEMBER(root) 模式
    // 条件用 ; 分隔，是 “或” 的关系
    private boolean __is_matched_role_of_group(WnSystem sys, String pvg) {
        String[] ss = Strings.splitIgnoreBlank(pvg, ";");
        WnAccount me = sys.getMe();
        for (String s : ss) {
            Matcher m = _P.matcher(s);
            // 错误的输入，被认为是无效
            if (!m.find())
                continue;
            // 来吧，得到组名列表和权限名
            String roleName = m.group(1);
            String[] grps = Strings.splitIgnoreBlank(m.group(2));
            // 判断
            if ("ADMIN".equals(roleName)) {
                if (sys.auth.isAdminOfGroup(me, grps))
                    return true;
            }
            // 那就一定是成员咯
            else {
                if (sys.auth.isMemberOfGroup(me, grps))
                    return true;
            }

        }
        return false;
    }

    // rwx 模式
    private boolean __is_has_right_to_access(WnObj o, WnContext wc, String pvg) {
        int mod = 0;
        if (pvg.indexOf('r') >= 0)
            mod |= Wn.Io.R;
        if (pvg.indexOf('w') >= 0)
            mod |= Wn.Io.W;
        if (pvg.indexOf('x') >= 0)
            mod |= Wn.Io.X;
        if (mod > 0) {
            return wc.testSecurity(o, mod);
        }
        return true;
    }

    private WnObj __find_ftype(WnSystem sys, WnObj o, WnObj oUiHome) {
        WnObj oFType = null;
        // 文件夹
        if (o.isDIR()) {
            oFType = sys.io.fetch(oUiHome, "ftypes/" + Strings.sBlank(o.type(), "folder") + ".js");
        }
        // 文件
        else {
            // 有用 Type
            if (o.hasType()) {
                oFType = sys.io.fetch(oUiHome, "ftypes/" + o.type() + ".js");
            }
            // 找不到就用 MIME
            if (null == oFType) {
                WnObj oMimeHome = sys.io.fetch(oUiHome, "mimes");
                if (null != oMimeHome) {
                    String[] ss = Strings.splitIgnoreBlank(o.mime(), "/");
                    oFType = sys.io.fetch(oMimeHome, Lang.concat("_", ss) + ".js");
                    // 还找不到，就用 mime 的分类名
                    if (null == oFType) {
                        oFType = sys.io.fetch(oMimeHome, ss[0] + ".js");
                    }
                }
            }
        }
        // 没找到，那么就一定查找
        if (null == oFType) {
            oFType = sys.io.fetch(oUiHome, o.isDIR() ? "ftypes/folder.js" : "ftypes/_unknown.js");
        }
        return oFType;
    }
}
