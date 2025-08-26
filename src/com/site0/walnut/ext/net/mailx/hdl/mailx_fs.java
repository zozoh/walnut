package com.site0.walnut.ext.net.mailx.hdl;

import static com.site0.walnut.ext.net.mailx.util.Mailx.LOG;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.mailx.MailxContext;
import com.site0.walnut.ext.net.mailx.MailxFilter;
import com.site0.walnut.ext.net.mailx.bean.WnMimeMail;
import com.site0.walnut.ext.net.mailx.impl.WnMailRawRecieving;
import com.site0.walnut.ext.net.mailx.util.Mailx;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.tmpl.WnTmplX;

public class mailx_fs extends MailxFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(html)$");
    }

    @Override
    protected void process(WnSystem sys, MailxContext fc, ZParams params) {
        // 禁止发送
        fc.setQuiet(true);

        // 准备参数
        boolean showHeader = params.is("header");
        boolean isAutoDecrypt = params.is("decrypt");
        boolean isJson = params.is("json");
        String asContent = params.getString("content");
        String after = params.getString("after", null);
        NutMap fixedMeta = params.getMap("meta");

        // 得到输出目标
        WnTmplX taTmpl = Mailx.getTmpl(params, "to");

        // 附件的输出目标
        WnTmplX attachmentTmpl = Mailx.getTmpl(params, "at");

        // boolean hasTarget = null != oTa;
        boolean debug = null == taTmpl;
        debug = params.is("debug", debug);

        // 准备计时
        Stopwatch sw = Stopwatch.begin();

        // 读取邮件列表
        String srcDir = fc.config.fs.getHome();
        WnObj oSrcDir = Wn.getObj(sys, srcDir);

        // 得到收件箱文件夹名称
        String inbox = fc.config.fs.getInboxpPrefix(params.val(0));
        // 列出全部未处理邮件
        List<WnObj> list = sys.io.getChildren(oSrcDir, inbox);
        int N = list.size();
        // 循环处理消息
        Vector<WnObj> outputs = new Vector<>(N); // 如果输出为数据集，输出目标记录在这里

        // 循环处理消息
        for (int i = 0; i < N; i++) {
            try {
                WnObj o = list.get(i);
                // 它会自己在构造函数里创建一个空的 Session
                WnMailRawRecieving rv = new WnMailRawRecieving();
                rv.mimeText = sys.io.readText(o);
                rv.isAutoDecrypt = isAutoDecrypt;
                rv.sys = sys;
                rv.fc = fc;
                rv.asContent = asContent;
                rv.showHeader = showHeader;
                rv.debug = debug;
                rv.i = i;
                rv.N = N;
                rv.fixedMeta = fixedMeta;

                // 后续处理
                rv.taTmpl = taTmpl;
                rv.attachmentTmpl = attachmentTmpl;
                rv.after = after;
                rv.outputs = outputs;

                // 直接执行
                rv.run();

                // 移动到归档文件夹
                WnMimeMail mail = rv.getMimeMail();
                String newKeyFmt = fc.config.fs.getArchivePrefix();
                if (null != mail && !Ws.isBlank(newKeyFmt)) {
                    Date raDate = mail.getReceiveAtDate();
                    String newKey = Wtime.format(raDate, newKeyFmt);
                    String newPath = Wn
                        .appendPath(fc.config.fs.getHome(), newKey, o.name());
                    String aph = Wn.normalizeFullPath(newPath, sys);
                    sys.io.move(o, aph);
                    LOG(sys,
                        debug,
                        "     move to archive, o.id=%s => %s",
                        o.id(),
                        aph);
                }
            }
            // 出错就中断
            catch (Throwable e) {
                throw Er.wrap(e);
            }
        }

        sw.stop();
        LOG(sys,
            debug,
            "mailx_fs: Receiving done in %s: N=%d",
            sw.toString(),
            N);

        // 输出
        if (isJson) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            String json = Json.toJson(outputs, jfmt);
            sys.out.println(json);
        }

    }

}
