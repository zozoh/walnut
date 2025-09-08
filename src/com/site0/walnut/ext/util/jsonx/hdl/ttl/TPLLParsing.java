package com.site0.walnut.ext.util.jsonx.hdl.ttl;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.util.Ws;

public class TPLLParsing {

    private TPLLField[] fields;

    private TimeZone timezone;

    public TPLLParsing(TPLLField[] fields) {
        this.fields = fields;
    }

    public List<NutMap> parse(String input) {
        String[] lines = Ws.splitIgnoreBlank(input, "\r?\n");
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
                if (val != null) {
                    map.put(fld.getKey(), val);
                }
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
            re = Double.valueOf(val);
        }
        // 针对日期的类型
        else if (fld.isDts20()) {
            re = fld.parseAsDts20(timezone, val);
        }
        // 针对日期时间
        else if (fld.isDcymd8()) {
            re = fld.parseAsDcymd8(timezone, val);
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
