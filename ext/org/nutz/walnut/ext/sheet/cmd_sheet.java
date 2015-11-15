package org.nutz.walnut.ext.sheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_sheet extends JvmExecutor {

    private static Pattern _P = Pattern.compile("^([0-9a-zA-Z_.]+)(\\[)(.+)(\\])$");

    private static Pattern _P2 = Pattern.compile("^([$]n)([.]?)(.+)$");

    private static Pattern _P3 = Pattern.compile("^([$]date)((%)(.+))?$");

    class FKey {
        FKey(String str) {
            Matcher m = _P.matcher(str);
            if (m.find()) {
                this.key = m.group(1);
                // 看看是日期还是数组
                String s = m.group(3);
                Matcher m2 = _P2.matcher(s);

                // 数组
                if (m2.find()) {
                    this.isArray = true;
                    this.arrayKey = Strings.sBlank(m2.group(3), null);
                } else {
                    m2 = _P3.matcher(s);
                    // 日期
                    if (m2.find()) {
                        this.isDate = true;
                        this.dateFormat = Strings.sBlank(m2.group(4), "yyyy-MM-dd HH:mm:ss");
                    }
                    // 奇怪
                    else {
                        throw Er.create("e.cmd.sheet.invalidKey", str);
                    }
                }
            } else {
                this.isDate = false;
                this.isArray = false;
                this.arrayKey = null;
                this.key = str;
            }
            // this.isPathCall = this.key.contains(".");
        }

        boolean isDate;
        boolean isArray;
        String dateFormat;
        String arrayKey;
        String key;
        // boolean isPathCall;

        String getValue(NutMap obj) {
            Object v = obj.get(key);
            if (null != v) {
                // 数组
                if (isArray) {
                    if (null == arrayKey) {
                        if (v.getClass().isArray()) {
                            return Lang.concat(", ", (Object[]) v).toString();
                        }
                        return Lang.concat(", ", (Collection<?>) v).toString();
                    }
                    final String[] vals = new String[Lang.length(v)];
                    Lang.each(v, new Each<Map<String, Object>>() {
                        public void invoke(int index, Map<String, Object> ele, int length) {
                            NutMap map = NutMap.WRAP(ele);
                            Object val = Mapl.cell(map, arrayKey);
                            vals[index] = null == val ? "--" : val.toString();
                        }
                    });
                    return Lang.concat(", ", vals).toString();
                }
                // 日期
                else if (isDate) {
                    Date d = Castors.me().castTo(v, Date.class);
                    return Times.format(dateFormat, d);
                }
                // 其他作为字符串
                return Castors.me().castToString(v);
            }
            return null;
        }
    }

    class Fld {
        FKey[] keys;
        String title;
        String dft;
        String dft_blank;

        Fld(String str) {
            String[] ss = Strings.splitIgnoreBlank(str, ":");
            String[] sKeys = Strings.splitIgnoreBlank(ss[0], "[|][|]");
            this.keys = new FKey[sKeys.length];
            for (int i = 0; i < sKeys.length; i++)
                this.keys[i] = new FKey(sKeys[i]);
            this.title = ss.length > 1 ? Strings.sBlank(ss[1], this.keys[0].key) : this.keys[0].key;
            this.dft = ss.length > 2 ? ss[2] : "";
            // 分析两种默认值
            int pos = this.dft.indexOf("/");
            if (pos > 0) {
                this.dft_blank = Strings.trim(this.dft.substring(pos + 1));
                this.dft = Strings.trim(this.dft.substring(0, pos));
            } else {
                this.dft = Strings.trim(this.dft);
                this.dft_blank = this.dft;
            }
        }

        String getValue(NutMap obj) {
            String re = null;
            for (int i = 0; i < keys.length; i++) {
                re = keys[i].getValue(obj);
                if (null != re)
                    break;
            }

            if (null == re)
                return this.dft;

            if (Strings.isBlank(re))
                return this.dft_blank;

            return re;
        }
    }

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "^(noheader)$");
        String type = params.get("mode", "csv");

        if (!type.equals("csv")) {
            throw Er.create("e.cmd.sheet.unsupport_mode", type);
        }

        String sep = params.get("sep", ";");

        // 读取输入
        String json = sys.in.readAll();
        List<NutMap> list = Json.fromJsonAsList(NutMap.class, Strings.sBlank(json, "[]"));

        // 啥都没有就啥都不写
        if (list.isEmpty())
            return;

        // 读取字段列表
        List<Fld> fields = null;
        // 指定了字段列表
        if (params.has("flds")) {
            String str = params.check("flds");
            String[] sss = Strings.splitIgnoreBlank(str, "[,\n]");
            fields = new ArrayList<Fld>(sss.length);
            for (String sFld : sss) {
                Fld fld = new Fld(sFld);
                fields.add(fld);
            }
        }
        // 根据第一个元素总结出字段列表
        else {
            NutMap first = list.get(0);
            fields = new ArrayList<Fld>(first.size());
            for (Map.Entry<String, Object> en : first.entrySet()) {
                Fld fld = new Fld(en.getKey());
                fields.add(fld);
            }
        }

        // 如果为空，还是啥都不输出
        if (fields.isEmpty())
            return;

        Fld[] flds = fields.toArray(new Fld[fields.size()]);

        // 是否输出表头行
        if (!params.is("noheader")) {
            sys.out.printf("\"%s\"", flds[0].title);
            for (int i = 1; i < flds.length; i++) {
                sys.out.printf("%s\"%s\"", sep, flds[i].title);
            }
            sys.out.println();
        }

        // 写入输出
        for (NutMap ele : list) {
            sys.out.printf("\"%s\"", flds[0].getValue(ele));
            for (int i = 1; i < flds.length; i++) {
                sys.out.printf("%s\"%s\"", sep, flds[i].getValue(ele));
            }
            sys.out.println();
        }

    }

}
