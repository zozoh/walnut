package com.site0.walnut.ext.util.jsonx.hdl.ttl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Ws;

public class TPLLParsing {

    private TPLLField[] fields;

    private TimeZone timezone;

    private HashSet<String> rawTypes;

    private boolean ignoreNil;

    private boolean useRawAll;

    private boolean useRawNumeric;

    private boolean useRawDts20;

    private boolean useRawDcymd8;

    private boolean useRawDcymd16;

    public TPLLParsing(TPLLField[] fields) {
        this.fields = fields;
        this.rawTypes = new HashSet<>();
    }

    public void setupRawTypes(String str) {
        String[] ss = Ws.splitIgnoreBlank(str);
        if (null != ss) {
            for (String s : ss) {
                rawTypes.add(s.toLowerCase());
            }
        }
        this.useRawAll = rawTypes.contains("all");
        this.useRawDcymd8 = rawTypes.contains("dcymd8");
        this.useRawDcymd16 = rawTypes.contains("dcymd16");
        this.useRawDts20 = rawTypes.contains("dts20");
        this.useRawNumeric = rawTypes.contains("numeric");

    }

    public boolean isIgnoreNil() {
        return ignoreNil;
    }

    public void setIgnoreNil(boolean ignoreNil) {
        this.ignoreNil = ignoreNil;
    }

    public List<NutMap> parse(String input) {
        String[] lines = input.split("\r?\n");
        List<NutMap> re = new ArrayList<>(lines.length);

        // 循环每行实现代码
        for (String line : lines) {
            if (Ws.isBlank(line)) {
                continue;
            }

            NutMap map = new NutMap();

            for (TPLLField fld : fields) {
                if (fld.isFiller())
                    continue;
                Object val = parseField(line, fld);
                if (this.ignoreNil && val == null) {
                    continue;
                }
                map.put(fld.getKey(), val);
            }

            re.add(map);
        }

        return re;
    }

    private Object parseField(String line, TPLLField fld) {
        // 检查行长度是否足够
        int start = fld.getStart() - 1;
        if (line.length() <= start) {
            return null;
        }

        // 计算字段的实际结束位置
        int end = start + fld.getLen();
        if (end > line.length()) {
            end = line.length();
        }

        // 截取字段值并去除首尾空白
        String val = line.substring(start, end).trim();
        Object re = val;

        // 如果是空字符串，返回null
        if (Ws.isBlank(val)) {
            return null;
        }

        // 根据字段类型进行处理
        if (fld.isNumeric()) {
            if (useRawNumeric || useRawAll) {
                return re;
            }
            re = Double.valueOf(val);
        }
        // 针对日期的类型
        else if (fld.isDts20()) {
            if (useRawDts20 || useRawAll) {
                return re;
            }
            re = fld.parseAsDts20(timezone, val);
        }
        // 针对日期 8位
        else if (fld.isDcymd8()) {
            if (useRawDcymd8 || useRawAll) {
                return re;
            }
            re = fld.parseAsDcymd8(timezone, val);
        }
        // 针对日期时间 16位
        else if (fld.isDcymd16()) {
            if (useRawDcymd16 || useRawAll) {
                return re;
            }
            re = fld.parseAsDcymd16(timezone, val);
        }

        // 默认就是字符类
        return re;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

}
