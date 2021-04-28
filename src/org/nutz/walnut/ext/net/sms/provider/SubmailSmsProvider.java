package org.nutz.walnut.ext.net.sms.provider;

import java.io.IOException;

import org.nutz.http.Request;
import org.nutz.http.Sender;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.http.Request.METHOD;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.net.sms.SmsProvider;
import org.nutz.walnut.ext.net.sms.SmsQuery;
import org.nutz.walnut.ext.net.sms.SmsSend;

public class SubmailSmsProvider implements SmsProvider {

    @Override
    public String send(NutMap conf, SmsSend s) throws IOException {
        String url;
        NutMap params = new NutMap();
        params.put("appid", conf.get("appid"));
        params.put("signature", conf.get("appkey"));
        params.put("to", s.receiver);
        if (s.vars.isEmpty()) {
            // 暂不支持
            url = "https://api.mysubmail.com/message/send.json";
            return "{error:'not support yet'}";
        } else {
            url = "https://api.mysubmail.com/message/xsend.json";
            params.put("project", s.vars.get("project"));
            s.vars.remove("project");
            params.put("vars", Json.toJson(s.vars, JsonFormat.full()));
        }
        Request req = Request.create(url, METHOD.POST, params);
        return Sender.create(req).send().getContent();
    }

    @Override
    public String query(NutMap conf, SmsQuery q) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
