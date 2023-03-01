package org.nutz.walnut.ext.net.imap.hdl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.ThQuery;
import org.nutz.walnut.ext.net.imap.ImapContext;
import org.nutz.walnut.ext.net.imap.ImapFilter;
import org.nutz.walnut.ext.net.imap.bean.WnEmailMessage;
import org.nutz.walnut.ext.net.imap.bean.WnEmailPart;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class imap_save extends ImapFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn", "^(quiet)$");
    }

    @Override
    protected void process(WnSystem sys, ImapContext fc, ZParams params) {
        // 防守
        if (!fc.hasList()) {
            return;
        }

        // 得到目标
        String ph = params.val_check(0);
        WnObj oTs = Wn.checkObj(sys, ph);

        // 准备 Thing 服务类
        WnThingService wts = new WnThingService(sys, oTs);

        // 逐个创建对象
        List<WnObj> list = new ArrayList<>(fc.list.size());
        for (WnEmailMessage em : fc.list) {
            String msgId = em.getMessageId();

            // 首先去除重复，让对象名为消息 ID 是一个很好的选择
            NutMap meta = new NutMap();
            meta.put("nm", msgId);

            // 首先查询一下
            ThQuery q = new ThQuery(meta);
            WnObj oT = wts.getOne(q);
            if (null != oT) {
                list.add(oT);
                continue;
            }

            meta.put("tp", "email");
            meta.put("subject", em.getSubject());

            // 生成其他必要的字段
            meta.put("mail_from", em.getFromBeans());
            meta.put("mail_to", em.getToBeans());
            meta.put("mail_reply", em.getReplayToBeans());
            meta.put("mail_num", em.getNumber());
            meta.put("mail_folder", em.getFolder());
            meta.put("mail_size", em.getContentLength());

            meta.put("mime", em.getContentType());
            meta.put("ct", em.getReceivedDate().getTime());

            // 创建对象
            oT = wts.createThing(meta, "nm", sys);

            // 写入内容
            if (em.hasContent()) {
                sys.io.writeText(oT, em.getContent());
            }

            // 写入附件
            if (em.hasAttachment()) {
                for (WnEmailPart part : em.getAttachment()) {
                    String fnm = part.getFileName();
                    InputStream ins = part.getStream();
                    wts.fileAdd("attachment", oT, fnm, ins, null, true);
                }
            }

            // 计入结果
            list.add(oT);
        }

        // 输出结果
        if (!params.is("quiet")) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            String json = Json.toJson(list, jfmt);
            sys.out.println(json);
        }
    }

}
