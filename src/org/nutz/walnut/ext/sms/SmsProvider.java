package org.nutz.walnut.ext.sms;

import java.io.IOException;

import org.nutz.lang.util.NutMap;

public interface SmsProvider {

    String send(NutMap conf, SmsSend s) throws IOException;

    String query(NutMap conf, SmsQuery q) throws IOException;
}
