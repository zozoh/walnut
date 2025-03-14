package com.site0.walnut.util.tmpl.ele;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;

/**
 * 格式如下（有点复杂 ^_^!）
 * 
 * <pre>
 * ${path<:@处理器;@处理器'参数1','参数2';@处理器'参数1';:映射>}
 * </pre>
 * 
 * 例如
 * 
 * <pre>
 * ${path<:@trim;@replace'/','-';@replace'~';:0=A,1=B>}
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class TmplStringEle extends TmplDynamicEle {

    private List<StrEleConvertor> convertors;

    public TmplStringEle(String key, String fmt, String dft) {
        super(null, key, null, dft);
        this.fmt = Strings.sNull(fmt, null);
        parseFormat(this.fmt);
    }

    public void parseFormat(String fmt) {
        if (null == fmt) {
            convertors = null;
            return;
        }

        // 先拆分处理器
        String[] ss = Strings.split(this.fmt, true, ';');

        // 预处理字段
        this.convertors = new ArrayList<StrEleConvertor>(ss.length);
        for (String s : ss) {

            // 截取空白
            if (s.equals("@trim")) {
                convertors.add(new StrTrimConvertor());
            }
            // 截取字符串
            else if (s.startsWith("@sub=")) {
                String input = s.substring(5).trim();
                convertors.add(new StrSubConvertor(input));
            }
            // 字符串替换
            else if (s.startsWith("@replace")) {
                String input = s.substring("@replace".length());
                convertors.add(new StrReplaceConvertor(input));
            }
            // 限制长度和精度的浮点数 ${w<:@fs=7.2>} 表示最多7长度，小数点后最多两位
            // 12345678.45 => 1234567
            // 1234567.85 => 1234567
            // 123456.85 => 123456
            // 12348.45342 => 12348.4
            // 0.00000001 => 0.01
            else if (s.startsWith("@fs=")) {
                String input = s.substring("@fs=".length());
                convertors.add(new StrFloatSubStrConvertor(input));
            }
            // 字符串映射
            else if (s.startsWith(":")) {
                String input = s.substring(1);
                convertors.add(new StrMappingConvertor(input));
            }
            // 大小写转换
            else if (s.toLowerCase().matches("^(upper|lower|camel|kebab|snake)$")) {
                convertors.add(new StrCaseCovertor(s));
            }
            // 默认是字符串格式化
            else {
                convertors.add(new StrFormatConvertor(s));
            }
        }
    }

    @Override
    protected String _val(Object val) {
        if (null == val) {
            return null;
        }
        if (null != val) {
            if (val.getClass().isArray()) {
                return Wlang.concat(", ", (Object[]) val).toString();
            }
            if (val instanceof Collection<?>) {
                return Strings.join(", ", (Collection<?>) val);
            }
        }
        String re = val.toString();
        if (null != convertors && !convertors.isEmpty()) {
            for (StrEleConvertor co : convertors) {
                re = co.process(re);
            }
        }
        return re;
    }

}
