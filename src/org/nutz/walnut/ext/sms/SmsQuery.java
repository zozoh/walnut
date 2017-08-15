package org.nutz.walnut.ext.sms;

import java.util.Date;

import org.nutz.lang.util.NutMap;

public class SmsQuery {

    public Date from;

    public Date to;

    public String receiver;

    public int pageSize;

    public int pageNumber;

    public NutMap vars;

}
