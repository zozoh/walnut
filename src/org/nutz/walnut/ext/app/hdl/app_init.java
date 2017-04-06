package org.nutz.walnut.ext.app.hdl;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;

public class app_init implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // 得到要操作的帐号
        Wn.WC().security(new WnEvalLink(sys.io), new Atom() {
            @Override
            public void run() {
                __exec_without_security(sys, hc);
            }
        });

    }

    private void __exec_without_security(WnSystem sys, JvmHdlContext hc) {
        // 得到要操作的用户
        WnUsr me = sys.me;
        if (hc.params.has("u")) {
            me = sys.usrService.check(hc.params.get("u"));
        }

        boolean isME = me.isSameId(sys.me);

        // 如果操作的用户不是自己，必须是 root 或者 op 组成员才能做
        if (!isME) {
            if (!sys.usrService.isMemberOfGroup(sys.me, "root")
                && !sys.usrService.isMemberOfGroup(sys.me, "op")) {
                throw Er.create("e.cmd.app_init.nopvg");
            }
        }

        if (isME) {
            __exec_init(sys, hc);
        } else {
            // 为其创建会话, 切换到对应用户
            WnSession se = sys.sessionService.create(me);
            WnSession ose = sys.se;
            WnUsr ome = sys.me;
            sys.se = se;
            sys.me = me;
            try {
                __exec_init(sys, hc);
            }
            // 释放 session
            finally {
                sys.se = ose;
                sys.me = ome;
                sys.sessionService.logout(se.id());

            }
        }
    }

    private void __exec_init(WnSystem sys, JvmHdlContext hc) {
        // 得到关键目录
        String ph_tmpl = hc.params.val_check(0);
        String ph_dest = Strings.sBlank(hc.params.val(1), "~");

        WnObj oTmpl = Wn.checkObj(sys, ph_tmpl);
        WnObj oDest = Wn.checkObj(sys, ph_dest);

        // 得到上下文
        NutMap c;
        String json = hc.params.get("c");
        if (null != json && "true".equals(json)) {
            c = Json.fromJson(NutMap.class, json);
        } else {
            c = new NutMap();
        }

        // 优先处理文件结构
        WnObj oFileS = sys.io.fetch(oTmpl, "_files");
        if (null != oFileS) {
            String text = Strings.sBlank(sys.io.readText(oFileS), "");
            String[] lines = text.split("(\r?\n)+");

            sys.out.println("init files:");

            // 处理器
            FileProcess fp = new FileProcess().reset();

            // 准备段开始的正则表达式
            Pattern p_fp_start = Pattern.compile("@(DIR|FILE) +(.+)");
            Pattern p_fp_content = Pattern.compile("%(COPY|TMPL)([%>:])(.+)?");

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
                    Matcher m = p_fp_start.matcher(str);
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

                    Matcher m = p_fp_content.matcher(str);
                    // 遇到了内容描述行
                    if (m.find()) {
                        String md = m.group(1);
                        String tp = m.group(2);
                        String s = Strings.sBlank(m.group(3), "");

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
                    if ("}".equals(str)) {
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
            sys.out.println("run script:");
            String script = sys.io.readText(oScript);
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
            WnObj o = sys.io.createIfNoExists(oDest, rph, race);
            sys.out.printlnf(" + %4s : %s", race, rph);

            // 元数据
            if (null != meta && meta.size() > 0) {
                sys.out.printlnf("   > %d metas", meta.size());
                sys.io.appendMeta(o, meta);
            }

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
