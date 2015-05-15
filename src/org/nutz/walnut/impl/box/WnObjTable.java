package org.nutz.walnut.impl.box;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

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
            if (null == val) {
                cells.add("--");
                continue;
            }
            Mirror<?> mi = Mirror.me(val);
            // 尺寸
            if (key.equals("len")) {
                if (briefSize) {
                    cells.add(__len((Long) val, 1000, 0, 'B', 'K', 'M', 'G', 'T'));
                } else {
                    cells.add(val.toString());
                }
            }
            // 模式
            else if (key.equals("md")) {
                cells.add(__mode(o));
            }
            // 日期时间
            else if (key.matches("^lm|ct|expi$")) {
                cells.add(__tm((Long) val));
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
        // 对象
        else if (o.isOBJ()) {
            font = 3;
            color = 33;
        }
        // 文件
        else if (o.isFILE()) {
            font = o.isHidden() ? 2 : 0;
            // TODO 可执行文件，标个绿色
        }
        // 不可能
        else {
            throw Lang.impossible();
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
        case OBJ:
            sb.append('o');
            break;
        default:
            throw Lang.impossible();
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
        sb.append(Strings.alignRight(Times.format("M", lm), 2, ' '));
        sb.append(' ');
        sb.append(Strings.alignRight(Times.format("d", lm), 2, ' '));
        sb.append(' ');
        sb.append(Times.format("HH:mm", lm)).append(' ');
        return sb.toString();
    }

    public String toString() {
        return tt.toString();
    }

}
