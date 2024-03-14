package com.site0.walnut.ext.sys.mq.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.mq.WnMqApi;
import com.site0.walnut.ext.sys.mq.WnMqMessage;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.web.WnConfig;

public class mq_send implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 读取主题
        String topic = hc.params.get("t", "sys");
        // ......................................
        // 读取密钥
        WnObj oSecret = Wn.checkObj(sys, "~/.mq/secret");
        String skey = sys.io.readText(oSecret);
        if (Strings.isBlank(skey)) {
            throw Er.create("e.cmd.mq_send.NilSecret");
        }
        // ......................................
        // 得到接口
        WnConfig conf = hc.ioc.get(WnConfig.class, "conf");
        String beanName = conf.get("mq-api-name", "messageQueueApi");
        WnMqApi api = hc.ioc.get(WnMqApi.class, beanName);
        // ......................................
        // 读取消息
        int len = Math.min(1, hc.params.vals.length);
        List<String> list = new ArrayList<>(len);
        // 从管道读取
        if (hc.params.vals.length == 0) {
            String str = sys.in.readAll();
            list.add(str);
        }
        // 就是参数
        else {
            for (String str : hc.params.vals) {
                list.add(str);
            }
        }
        // ......................................
        // 循环解析消息，准备一个消息列表
        List<WnMqMessage> msgs = new ArrayList<>(list.size());
        for (String str : list) {
            // 无视空白
            if (Strings.isBlank(str)) {
                continue;
            }

            // 如果是文件对象，先读取一下，看看是 JSON还是TEXT
            if (str.startsWith("<-")) {
                String ph = str.substring(2).trim();
                WnObj o = Wn.checkObj(sys, ph);
                str = sys.io.readText(o);
            }

            // 准备消息对象
            WnMqMessage mqMsg = null;

            // JSON
            if (Strings.isQuoteBy(str, '{', '}')) {
                mqMsg = Json.fromJson(WnMqMessage.class, str);
            }
            // TEXT
            else {
                mqMsg = new WnMqMessage();
                mqMsg.parseText(str);
            }

            // 记入消息表
            if (mqMsg.hasBody()) {
                // 补充消息的默认字段
                mqMsg.setDefaultUser(sys.getMyName());
                mqMsg.setDefaultSecret(skey);

                // 嗯，加入消息队列吧
                msgs.add(mqMsg);
            }
        }
        // ......................................
        // 循环发送消息
        for (WnMqMessage mqMsg : msgs) {
            api.send(topic, mqMsg);
        }
    }

}
