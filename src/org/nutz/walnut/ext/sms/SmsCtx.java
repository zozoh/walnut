package org.nutz.walnut.ext.sms;

import org.nutz.json.JsonField;
import org.nutz.walnut.impl.box.WnSystem;

public class SmsCtx {

    @JsonField(ignore = true)
    public WnSystem sys;
    public String conf;
    public String lang;
    public String provider;
    public String header;
    public String msg;
    public String mobiles;
    public boolean debug;
}
