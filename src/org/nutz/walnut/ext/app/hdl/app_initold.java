package org.nutz.walnut.ext.app.hdl;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnSysConf;

public class app_initold implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到当前操作路径
        String pwd = sys.session.getVars().getString("PWD");

        // 得到要操作的帐号
        Wn.WC().security(new WnEvalLink(sys.io), new Atom() {
            @Override
            public void run() {
                __exec_without_security(sys, hc);
            }
        });

        // 恢复到当前操作路径
        sys.exec("cd '" + pwd + "'");

    }

    private void __exec_without_security(WnSystem sys, JvmHdlContext hc) {
        // 得到要操作的用户
        WnAccount me = sys.getMe();
        WnAccount u = sys.getMe();
        if (hc.params.has("u")) {
            String unm = hc.params.get("u");
            u = sys.auth.checkAccount(unm);
        }

        boolean isME = u.isSame(me);

        // 如果操作的用户不是自己，必须是 root 或者 op 组成员才能做
        if (!isME) {
            if (!sys.auth.isMemberOfGroup(me, "root", "op")) {
                throw Er.create("e.cmd.app_init.nopvg");
            }
        }

        // 本人
        if (isME) {
            __exec_init(sys, hc);
        }
        // 为其创建会话, 切换到对应用户
        else {
            sys.switchUser(u, new Callback<WnSystem>() {
                public void invoke(WnSystem sys2) {
                    __exec_init(sys2, hc);
                }
            });
        }
    }

    protected void __exec_init(WnSystem sys, JvmHdlContext hc) {
        // 得到关键目录
        WnObj oTmpl, oDest;
        // 自动模式
        if (hc.params.vals.length <= 1) {
            String ph_dest = Strings.sBlank(hc.params.val(0), "~");
            oDest = Wn.checkObj(sys, ph_dest);

            // 这个要寻找一下
            String grp = sys.getMyGroup();
            WnObj oMntHome = sys.io.check(null, "/mnt/project/" + grp);
            oTmpl = sys.io.fetch(oMntHome, "init/domain");
            // 向下找一层看看
            if (null == oTmpl) {
                List<WnObj> children = sys.io.getChildren(oMntHome, null);
                for (WnObj oChild : children) {
                    if (!oChild.isDIR()) {
                        continue;
                    }
                    oTmpl = sys.io.fetch(oChild, "init/domain");
                    if (null != oTmpl) {
                        break;
                    }
                }
            }
        }
        // 指定模式
        else {
            String ph_tmpl = hc.params.val_check(0);
            String ph_dest = Strings.sBlank(hc.params.val(1), "~");
            oTmpl = Wn.checkObj(sys, ph_tmpl);
            oDest = Wn.checkObj(sys, ph_dest);
        }

        sys.out.println("tmpl : " + oTmpl.getRegularPath());
        sys.out.println("dest : " + oDest.getRegularPath());

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
            WnObj tmp = sys.io.fetch(null, Wn.normalizeFullPath("~/.domain/vars.json", sys));
            if (tmp == null)
                c = new NutMap();
            else {
                sys.out.println("read vars from ~/.domain/vars.json");
                c = sys.io.readJson(tmp, NutMap.class);
            }
        }

        // 上下文一定要增加的键
        WnSysConf sysconf = sys.getSysConf();
        c.putDefault("hostAndPort", sysconf.getMainHostAndPort());
        c.putDefault("scheme", sysconf.getMainScheme());
        c.putDefault("urlbase", sysconf.getMainUrlBase());
        c.put("sys", sysconf);
        c.put("me", sys.getMyName());

        // 优先处理文件结构
        WnObj oFileS = sys.io.fetch(oTmpl, "_files");
        if (null != oFileS) {
            String text = Strings.sBlank(sys.io.readText(oFileS), "");
            String[] lines = text.split("(\r?\n)+");

            sys.out.println("init files:");

            // 处理器
            FileProcess fp = new FileProcess().reset();

            // 准备段开始的正则表达式
            Pattern P_R = Pattern.compile("@(DIR|FILE) +(.+)");
            Pattern P_C = Pattern.compile("([%?])(COPY|TMPL)([%>:])(.+)?");

            // 处理每一行
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                String trim = Strings.trim(line);
                // 忽略注释和空行
                if (trim.startsWith("#") || Strings.isEmpty(trim))
                    continue;

                // 处理模板行
                String str = Tmpl.exec(trim, c, true);

                // 一直寻找到正确的开始,看看是否开始一个对象
                if (fp.is(FpSt.BLANK, FpSt.WAIT_META, FpSt.END_META, FpSt.DONE)) {
                    Matcher m = P_R.matcher(str);
                    if (m.find()) {
                        // 执行之前的处理器
                        fp.process(sys, oTmpl, oDest, c).reset();

                        // 设置值
                        fp.race = WnRace.valueOf(m.group(1));
                        fp.rph = m.group(2);
                        fp.st = FpSt.WAIT_META;

                        // 继续
                        continue;
                    }
                }

                // 等待元数据
                if (fp.is(FpSt.WAIT_META, FpSt.END_META)) {
                    // 遇到元数据开始
                    if ("{".equals(str)) {
                        fp.json.append(str);
                        fp.st = FpSt.IN_META;
                        continue;
                    }

                    Matcher m = P_C.matcher(str);
                    // 遇到了内容描述行
                    if (m.find()) {
                        String fc = m.group(1);
                        String md = m.group(2);
                        String tp = m.group(3);
                        String s = Strings.sBlank(m.group(4), "");

                        // 强制
                        fp.force = "%".equals(fc);

                        // 类型
                        fp.asFile = tp.equals(">");

                        // 如果纯粹的文本内容，继续向下读取
                        if (!fp.asFile && ":".equals(tp)) {
                            for (i++; i < lines.length; i++) {
                                String l2 = lines[i];
                                // 结束
                                if ("%END%".equals(Strings.trim(l2))) {
                                    break;
                                }
                                // 累计
                                s += l2 + "\n";
                            }
                        }

                        // COPY
                        if ("COPY".equals(md)) {
                            fp.copyBy = Strings.trim(s);
                        }
                        // TMPL
                        else {
                            fp.tmplBy = Strings.trim(s);
                        }

                        // 标识
                        fp.st = FpSt.DONE;
                        continue;
                    }

                }

                // 在元数据中
                if (fp.is(FpSt.IN_META)) {
                    // 遇到元数据结束
                    if ("}".equals(str) && line.startsWith("}")) {
                        fp.json.append(str);
                        fp.meta = Json.fromJson(NutMap.class, fp.json);
                        fp.st = FpSt.END_META;
                        continue;
                    }

                    // 在之内加入
                    fp.json.append(str);
                }

            }

            // 执行最后一个处理器
            fp.process(sys, oTmpl, oDest, c);
        }

        // 最后处理脚本
        WnObj oScript = sys.io.fetch(oTmpl, "_script");
        if (null != oScript) {
            String script = sys.io.readText(oScript);
            script = Tmpl.exec(script, c);
            sys.out.printlnf("run script:\n%s", script);
            sys.exec(script);
        }
    }

    /**
     * <pre>
       +----------------------------------------+
       V                                        |
    blank -+-> waitMeta -> inMeta -> endMeta ->done
           |      |                    ^        ^
           |      +---------------------        |
           +------------------------------------+
     * </pre>
     */
    static enum FpSt {
        BLANK, WAIT_META, IN_META, END_META, DONE
    }

    // _files 的处理段
    static class FileProcess {
        FpSt st;
        WnRace race;
        String rph;
        StringBuilder json;
        NutMap meta;
        String copyBy;
        String tmplBy;
        boolean asFile;
        boolean force;

        FileProcess reset() {
            st = FpSt.BLANK;
            race = null;
            rph = null;
            json = new StringBuilder();
            meta = null;
            copyBy = null;
            tmplBy = null;
            asFile = false;
            return this;
        }

        FileProcess process(WnSystem sys, WnObj oTmpl, WnObj oDest, NutMap c) {
            // 无效的，忽略
            if (FpSt.BLANK == st)
                return this;

            // 创建对象
            String the_rph = Tmpl.exec(rph, c);
            WnObj o = sys.io.createIfNoExists(oDest, the_rph, race);
            sys.out.printlnf(" + %4s : %s", race, the_rph);

            // 元数据
            if (null != meta && meta.size() > 0) {
                sys.out.printlnf("   > %d metas", meta.size());
                sys.io.appendMeta(o, meta);
            }

            // 如果存在，且内容 > 0，不强制写入
            if (this.force || o.len() == 0) {
                // Copy 内容
                if (!Strings.isBlank(copyBy)) {
                    // 文件
                    if (this.asFile) {
                        sys.out.printlnf("   <- %s", copyBy);
                        WnObj oSrc = sys.io.check(oTmpl, copyBy);
                        InputStream ins = sys.io.getInputStream(oSrc, 0);
                        sys.io.writeAndClose(o, ins);
                    }
                    // 文本内容
                    else {
                        sys.out.printlnf("   << %d chars", copyBy.length());
                        sys.io.writeText(o, copyBy);
                    }
                }
                // 执行模板
                else if (!Strings.isBlank(tmplBy)) {
                    String tmpl;
                    // 文件
                    if (this.asFile) {
                        sys.out.printlnf("   $- %s", tmplBy);
                        WnObj oSrc = sys.io.check(oTmpl, tmplBy);
                        tmpl = sys.io.readText(oSrc);
                    }
                    // 文本内容
                    else {
                        sys.out.printlnf("   $$ %d chars", tmplBy.length());
                        tmpl = tmplBy;
                    }
                    // 写入
                    String text = Tmpl.exec(tmpl, c);
                    sys.io.writeText(o, text);
                }
            }

            // 返回自身以便链式赋值
            return this;
        }

        boolean is(FpSt... sts) {
            for (FpSt st : sts)
                if (this.st == st)
                    return true;
            return false;
        }
    }
}
