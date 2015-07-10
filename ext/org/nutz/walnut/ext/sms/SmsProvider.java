package org.nutz.walnut.ext.sms;

import java.io.IOException;

import org.nutz.lang.util.NutMap;

public interface SmsProvider {

    String send(NutMap conf, String msg, String to) throws IOException;
}
