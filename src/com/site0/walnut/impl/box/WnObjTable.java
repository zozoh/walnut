package com.site0.walnut.impl.box;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wtime;

public class WnObjTable {

    private TextTable tt;

    private String[] keys;

    public WnObjTable(String keys) {
        this.keys = null == keys ? new String[0] : Strings.splitIgnoreBlank(keys);
        this.tt = new TextTable(this.keys.length + 1);
        // 搞搞居左居右
        for (int i = 0; i < this.keys.length; i++) {
            if (this.keys[i].matches("^lm|ct|expi|len$")) {
                tt.alignRigth(i);
            }
        }
    }

    public void add(WnObj o, boolean useColor, boolean briefSize) {
        List<String> cells = new ArrayList<String>(keys.length + 1);
        // 添加字段
        for (String key : keys) {
            Object val = o.get(key);

            // 尺寸
            if (key.equals("len")) {
                if (null == val) {
                    val = 0L;
                }
                if (briefSize) {
                    cells.add(__len((Long) val, 1000, 0, 'B', 'K', 'M', 'G', 'T'));
                } else {
                    cells.add(val.toString());
                }
                continue;
            }

            // 剩下的，判断一下空值
            if (null == val) {
                cells.add("--");
                continue;
            }
            Mirror<?> mi = Mirror.me(val);

            // 模式
            if (key.equals("md")) {
                cells.add(__mode(o));
            }
            // 日期时间
            else if (key.matches("^lm|ct|expi$")) {
                long ams = Wtime.parseAnyAMS(val);
                cells.add(__tm((ams)));
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
        // 最后添加名称
        StringBuilder sb = new StringBuilder();
        if (useColor)
            sb.append(__nm(o));
        else
            sb.append(o.name());

        // 如果是多列模式，看看最后是不是要输出 link 和 标签
        if (keys.length > 0) {
            if (o.isLink()) {
                sb.append(" -> ").append(o.link());
            }
            String[] lbls = o.labels();
            if (null != lbls && lbls.length > 0) {
                sb.append(LinuxTerminal.wrapFont(" #", 0, 33));
                for (String lb : lbls) {
                    sb.append(LinuxTerminal.wrapFont("[" + lb + "]", 0, 33, 43));
                }
            }
        }

        // 计入单元格
        cells.add(sb.toString());

        // 加入行
        tt.addRow(cells);
    }

    private String __nm(WnObj o) {
        String nm = o.name();
        // 没有管道输出，则考虑输出颜色
        int font = -1;
        int color = -1;

        // 链接
        if (o.isLink()) {
            font = o.isHidden() ? 2 : 1;
            color = 35;
        }
        // 目录
        else if (o.isDIR()) {
            font = o.isHidden() ? 2 : 1;
            color = 34;
        }
        // 文件
        else if (o.isFILE()) {
            font = o.isHidden() ? 2 : 0;
            // TODO 可执行文件，标个绿色
        }
        // 不可能
        else {
            throw Wlang.impossible();
        }

        // 输出颜色
        return LinuxTerminal.wrapFont(nm, font, color);
    }

    private String __mode(WnObj o) {
        StringBuilder sb = new StringBuilder();
        // RACE
        switch (o.race()) {
        case DIR:
            sb.append('d');
            break;
        case FILE:
            sb.append('-');
            break;
        default:
            throw Wlang.impossible();
        }

        // Mode
        sb.append(Wn.Io.modeToStr(o.mode())).append(' ');

        // 返回
        return sb.toString();
    }

    private String __len(long sz, long d, int uoff, char... unit) {
        if (sz < d || uoff >= unit.length - 1)
            return sz + "" + unit[uoff];
        return __len(sz / d, d, uoff + 1, unit);
    }

    private String __tm(long ms) {
        StringBuilder sb = new StringBuilder();
        Date lm = new Date(ms);
        sb.append(Strings.alignRight(Wtime.format(lm, "M"), 2, ' '));
        sb.append(' ');
        sb.append(Strings.alignRight(Wtime.format(lm, "d"), 2, ' '));
        sb.append(' ');
        sb.append(Wtime.format(lm, "HH:mm")).append(' ');
        return sb.toString();
    }

    public String toString() {
        return tt.toString();
    }

}
