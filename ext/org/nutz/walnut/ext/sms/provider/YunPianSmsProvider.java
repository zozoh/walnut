package org.nutz.walnut.ext.sms.provider;

import java.io.IOException;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.sms.SmsProvider;

public class YunPianSmsProvider implements SmsProvider {

    public String send(NutMap conf, String msg, String mobile) throws IOException {
        String header = conf.getString("header");
        if (header != null) {
            if (!msg.startsWith(header))
                msg = header + msg;
        }
        return YunpianSmsApi.sendSms(conf.getString("apikey"), msg, mobile);
    }

}
