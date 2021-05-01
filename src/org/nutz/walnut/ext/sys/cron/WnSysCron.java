package org.nutz.walnut.ext.sys.cron;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Ws;

public class WnSysCron {

    private String id;

    private String cron;

    private String user;

    private String command;

    private WnObj meta;

    public WnSysCron() {}

    public WnSysCron(String cron, String user, String command) {
        this.cron = cron;
        this.user = user;
        this.command = command;
    }

    public WnSysCron(WnObj o) {
        this.updateBeanFields(o);
    }

    public String toString() {
        return String.format("%s@%s: %s:\n%s", id, user, cron, command);
    }

    public String toBrief() {
        StringBuilder sb = new StringBuilder();
        // ID
        if (null != id) {
            sb.append(id.substring(0, 4));
        }
        // 用户
        if (null != user) {
            __join_str(sb, user, 4);
        }
        // 命令
        if (null != command) {
            __join_str(sb, command, 8);
        }
        // 搞定
        return sb.toString();
    }

    private void __join_str(StringBuilder sb, String str, int n) {
        sb.append(':');
        if (str.length() > n) {
            sb.append(str.substring(0, n - 1));
            sb.append('.');
        } else {
            sb.append(str);
        }
    }

    public void updateBeanFields(NutBean bean) {
        bean.put("tp", "cron_task");
        bean.put("cron", cron);
        bean.put("user", user);
    }

    public void loadFromObj(WnObj o) {
        this.meta = o;
        this.id = o.OID().getMyId();
        this.cron = o.getString("cron");
        this.user = o.getString("user");
        this.command = o.getString("content");
    }

    public boolean isValid() {
        return this.hasCron() && this.hasUser() && this.hasCommand();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasCron() {
        return !Ws.isBlank(cron);
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public boolean hasUser() {
        return !Ws.isBlank(user);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean hasCommand() {
        return !Ws.isBlank(command);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public WnObj getMeta() {
        return meta;
    }

    public void setMeta(WnObj meta) {
        this.meta = meta;
    }

}
