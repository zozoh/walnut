package org.nutz.walnut.ext.sms;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.JsonField;
import org.nutz.walnut.impl.box.WnSystem;

public class SmsCtx {

    @JsonField(ignore=true)
    public WnSystem sys;
    public String conf;
    public String provider;
    public String msg;
    public List<String> mobiles = new ArrayList<>();
    public boolean debug;
}
