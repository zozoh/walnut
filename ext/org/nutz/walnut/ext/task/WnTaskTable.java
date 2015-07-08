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

    public void add(WnObj o) {
        List<String> cells = new ArrayList<String>(keys.length + 1);
        // 添加字段
        for (String key : keys) {
            Object val = o.get(key);

            // 剩下的，判断一下空值
            if (null == val) {
                cells.add("--");
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
            // 简单对象
            else if (mi.isSimple()) {
                cells.add(val.toString());
            }
            // 其他变 JSON
            else {
                cells.add(Json.toJson(val, JsonFormat.compact().setQuoteName(false)));
            }
        }

        // 加入行
        tt.addRow(cells);
    }

    public String toString() {
        return tt.toString();
    }

}
