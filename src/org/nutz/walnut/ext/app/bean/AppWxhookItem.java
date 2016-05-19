package org.nutz.walnut.ext.app.bean;

import java.util.List;

import org.nutz.lang.Lang;

public class AppWxhookItem {

    public String id;

    public Object match;

    public boolean context;

    public Object command;

    @SuppressWarnings("unchecked")
    void addCommand(String cmd) {
        if (null == command) {
            command = cmd;
        } else if (command instanceof String) {
            command = Lang.list(command.toString(), cmd);
        } else {
            ((List<String>) command).add(cmd);
        }
    }

}
