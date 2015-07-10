package org.nutz.walnut.ext.email;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.JsonField;
import org.nutz.walnut.impl.box.WnSystem;

public class MailCtx {

    @JsonField(ignore=true)
    public WnSystem sys;
    public String config;
    public List<String> receivers = new ArrayList<>();
    public List<String> cc = new ArrayList<>();
    public String subject;
    public String tmpl;
    public String vars;
    public String msg;
    public List<String> attachs = new ArrayList<>();
    public boolean debug;
    public boolean local;
    public String sender;
}
