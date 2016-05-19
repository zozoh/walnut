package org.nutz.walnut.ext.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.LinuxTerminal;
import org.nutz.walnut.impl.box.TextTable;

public class WnTaskTable {
    private TextTable tt;

    private String[] keys;

    public WnTaskTable(String keys) {
        this.keys = null == keys ? new String[0] : Strings.splitIgnoreBlank(keys);
        this.tt = new TextTable(this.keys.length + 1);
        // 搞搞居左居右
        for (int i = 0; i < this.keys.length; i++) {
            if (this.keys[i].matches("^lm|ct|expi|len$")) {
                tt.alignRigth(i);
            }
        }
    }

    public void add(int indent, WnObj o, boolean nocolor) {
        List<String> cells = new ArrayList<String>(keys.length + 1);
        String st = o.getString("status");

        // 添加字段
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            Object val = o.get(key);

            // ow 字段显示默认值
            if ("ow".equals(key)) {
                String ow;

                if (null == val) {
                    ow = "N/A";
                    if (!nocolor)
                        ow = LinuxTerminal.wrapFont(ow, 2, 37);
                } else {
                    ow = val.toString();
                    if (!nocolor)
                        ow = LinuxTerminal.wrapFont(ow, 0, 37);
                }

                cells.add(ow);
                continue;
            }

            // 剩下的，判断一下空值
            if (null == val) {
                cells.add("");
                continue;
            }
            Mirror<?> mi = Mirror.me(val);

            // 日期时间
            if (key.matches("^lm|ct|expi|d_start|d_stop$")) {
                SimpleDateFormat df = new SimpleDateFormat("yy-MM-ddTHH:mm:ss");
                if (o.has("tzone")) {
                    TimeZone tz = TimeZone.getTimeZone(o.getString("tzone"));
                    df.setTimeZone(tz);
                }
                Date d = new Date((Long) val);
                cells.add(df.format(d));
            }
            // 值是日期对象
            else if (mi.isDateTimeLike()) {
                cells.add(Castors.me().castToString(val));
            }
            // 标题和状态，需要安装状态输出颜色
            else if (key.matches("^title|status$")) {
                String str = val.toString();
                if (!nocolor) {
                    str = __fmt_by_status(str, st);
                }

                // 如果是标题，看看有木有必要输出标签
                if ("title".equals(key)) {
                    String[] lbls = o.labels();
                    if (null != lbls & lbls.length > 0) {
                        str += " ";
                        for (String lbl : lbls) {
                            if (nocolor) {
                                str += "[" + lbl + "]";
                            } else {
                                str += " " + LinuxTerminal.wrapFont(lbl, 0, 30, 43);
                            }
                        }
                    }
                }

                // 记入
                cells.add(str);
            }
            // 简单对象
            else if (mi.isSimple()) {
                String str = val.toString();
                if (!nocolor)
                    str = LinuxTerminal.wrapFont(str, 2, 37);
                cells.add(str);
            }
            // 其他变 JSON
            else {
                String str = Json.toJson(val, JsonFormat.compact().setQuoteName(false));
                if (!nocolor)
                    str = LinuxTerminal.wrapFont(str, 0, 37);
                cells.add(str);
            }
        }

        // 首行需要缩进
        if (indent > 0) {
            String str = Strings.dup("  ", indent) + "- " + cells.get(0);
            cells.set(0, str);
        }

        // 加入行
        tt.addRow(cells);
    }

    private String __fmt_by_status(String str, String st) {
        // NEW | REOPEN | ACCEPT
        if (st.equals("NEW")) {
            str = LinuxTerminal.wrapFont(str, 0, 33);
        } else if (st.matches("^REOPEN|ACCEPT$")) {
            str = LinuxTerminal.wrapFont(str, 0, 37);
        }
        // PAUSE
        else if (st.equals("PAUSE")) {
            str = LinuxTerminal.wrapFont(str, 3, 31);
        }
        // ING
        else if (st.equals("ING")) {
            str = LinuxTerminal.wrapFont(str, 1, 34);
        }
        // DONE
        else if (st.matches("^DONE$")) {
            str = LinuxTerminal.wrapFont(str, 1, 32);
        }
        // 其他
        else {
            str = LinuxTerminal.wrapFont(str, 0, 36);
        }
        return str;
    }

    public String toString() {
        return tt.toString();
    }

}
