package com.site0.walnut.ext.net.sms.provider;

import java.io.IOException;

import org.nutz.http.Http;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.net.sms.SmsProvider;
import com.site0.walnut.ext.net.sms.SmsQuery;
import com.site0.walnut.ext.net.sms.SmsSend;

public class YunPianSmsProvider implements SmsProvider {

    private static String URI_SEND = "http://yunpian.com/v1/sms/send.json";

    private static String URI_QUERY = "https://sms.yunpian.com/v2/sms/get_record.json";

    @Override
    public String send(NutMap conf, SmsSend s) throws IOException {
        // 云片网要求短消息必须有头
        String header = conf.getString("header");
        if (!Strings.isBlank(header)) {
            if (!s.message.startsWith(header))
                s.message = header + s.message;
        }
        // 准备发送参数
        NutMap params = new NutMap();
        params.put("apikey", conf.getString("apikey"));
        params.put("text", s.message);
        params.put("mobile", s.receiver);

        // 补充扩展内容
        if (s.vars != null) {
            params.putAll(s.vars.pick("uid", "extend", "callback_url", "register", "mobile_stat"));
        }

        // 执行发送
        int timeout = conf.getInt("timeout", 5 * 1000);
        return Http.post(URI_SEND, params, timeout);
    }

    public String query(NutMap conf, SmsQuery q) throws IOException {
        // 云片网要求查询必须有 start_time / end_time
        NutMap params = new NutMap();
        params.put("start_time", Times.format("yyyy-MM-dd HH:mm:ss", q.from));
        params.put("end_time", Times.format("yyyy-MM-dd HH:mm:ss", q.to));

        // 准备参数
        params.put("apikey", conf.getString("apikey"));

        if (!Strings.isBlank(q.receiver)) {
            params.put("mobile", q.receiver);
        }

        if (q.pageNumber > 0)
            params.put("page_num", q.pageNumber);

        if (q.pageSize > 0)
            params.put("page_size", q.pageSize);

        if (q.vars != null && q.vars.has("type")) {
            params.put("type", q.vars.get("type"));
        }

        // 执行发送
        int timeout = conf.getInt("timeout", 5 * 1000);
        return Http.post(URI_QUERY, params, timeout);
    }

}
