package com.site0.walnut.ext.net.sms;

import org.nutz.json.JsonField;
import com.site0.walnut.impl.box.WnSystem;

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
