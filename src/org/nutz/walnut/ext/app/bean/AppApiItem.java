package org.nutz.walnut.ext.app.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutMap;

public class AppApiItem {

    public String path;

    public Map<String, String> headers;

    public NutMap metas;
    
    public String when;

    public List<String> commands;

    void addCommand(String cmd) {
        if (null == commands) {
            commands = new LinkedList<String>();
        }
        commands.add(cmd);
    }

}
