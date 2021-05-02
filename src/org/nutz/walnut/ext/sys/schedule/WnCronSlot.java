package org.nutz.walnut.ext.sys.schedule;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Times;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.cron.WnSysCron;

public class WnCronSlot {

    private String id;

    private String name;

    private String cron;

    private Date date;

    private int slot;

    private String task;

    private String user;

    private long expi;

    private String command;

    private WnObj meta;

    public WnCronSlot() {}

    public WnCronSlot(WnSysCron wsc) {
        this.task = wsc.getId();
        this.cron = wsc.getCron();
        this.user = wsc.getUser();
        this.command = wsc.getCommand();
    }

    public WnCronSlot(String input) {
        this.fromString(input);
    }

    public WnCronSlot(WnObj o) {
        this.loadFromObj(o);
    }

    public void updateBeanFields(NutBean bean) {
        bean.put("tp", "schedule_task");
        bean.put("cron", cron);
        bean.put("date", date.getTime());
        bean.put("slot", slot);
        bean.put("task", task);
        bean.put("user", user);
        bean.put("expi", expi);
    }

    public void loadFromObj(WnObj o) {
        this.meta = o;
        this.id = o.OID().getMyId();
        this.cron = o.getString("cron");
        this.setDate(o.getLong("date"));
        this.slot = o.getInt("slot");
        this.task = o.getString("task");
        this.user = o.getString("user");
        this.expi = o.expireTime();
        this.command = o.getString("content");
    }

    public String toString() {
        return String.format("%s:%s:%s", this.autoName(), user, cron);
    }

    public void fromString(String input) {
        String REG = "(\\d{8})-(\\d{1,4})-([^:]+):([^:]+):(.+)$";
        Pattern P = Pattern.compile(REG);
        Matcher m = P.matcher(input);
        /**
         * <pre>
         * 0/42  Regin:0/42
         * 0:[  0, 42) `20210913-0003-ea23..89a1:zozoh:0 0 0 * * ?`
         * 1:[  0,  8) `20210913`
         * 2:[  9, 13) `0003`
         * 3:[ 14, 24) `ea23..89a1`
         * 4:[ 25, 30) `zozoh`
         * 5:[ 31, 42) `0 0 0 * * ?`
         * </pre>
         */
        if (m.find()) {
            this.date = Times.D(m.group(1));
            this.slot = Integer.parseInt(m.group(2));
            this.task = m.group(3);
            this.user = m.group(4);
            this.cron = m.group(5);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        if (null == name) {
            this.autoName();
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String autoName() {
        this.name = String.format("%s-%04d-%s", getDateStr(), slot, task);
        return this.name;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Date getDate() {
        return date;
    }

    public String getDateStr() {
        if (null != date) {
            return Times.format("yyyyMMdd", date);
        }
        return "";
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(long ms) {
        this.date = new Date(ms);
    }

    public void setDate(String s) {
        this.date = Times.D(s);
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getExpi() {
        return expi;
    }

    public void setExpi(long expi) {
        this.expi = expi;
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
