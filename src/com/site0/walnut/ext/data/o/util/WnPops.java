package com.site0.walnut.ext.data.o.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.Regex;
import com.site0.walnut.ext.data.o.impl.pop.*;
import com.site0.walnut.util.Ws;

public class WnPops {

    public <T extends Object> List<T> exec(List<T> list, String pop) {
        WnPop po = parse(pop);
        return po.exec(list);
    }

    public static WnPop parse(String input) {
        // # - nil : 删除全部空数据项目
        if (null == input || "<nil>".equals(input)) {
            return new PopNil();
        }
        // # - all : 清空全部数据
        if ("<all>".equals(input)) {
            return new PopAll();
        }
        // # - 3 : 从后面弹出最多三个
        // # - -1 : 从开始处弹出最多一个
        Pattern p = Regex.getPattern("^(-?\\d+)$");
        Matcher m = p.matcher(input);
        if (m.find()) {
            int n = Integer.parseInt(input);
            return new PopN(n);
        }
        // # - i3 : 0 base下标，即第四个
        // # - i-1 : 最后一个
        // # - i-2 : 倒数第二个
        p = Regex.getPattern("^i(-?\\d+)$");
        m = p.matcher(input);
        if (m.find()) {
            int index = Integer.parseInt(m.group(1));
            return new PopIndex(index);
        }
        // # - =xyz : 弹出内容为 'xyz' 的项目
        // # - !=xyz : 弹出内容不为 'xyz' 的项目
        p = Regex.getPattern("^(!)?=(.+)$");
        m = p.matcher(input);
        if (m.find()) {
            String str = m.group(2);
            PopMatch pm = new PopEquals(str);
            pm.setNot("!".equals(m.group(1)));
            return pm;
        }
        // # - [a,b] : 弹出半角逗号分隔的列表里的值
        // # - ![a,b] : 弹出不在半角逗号分隔的列表里的值
        p = Regex.getPattern("^(!)?\\[([^\\]]*)\\]$");
        m = p.matcher(input);
        if (m.find()) {
            String str = Ws.trim(m.group(2));
            PopMatch pm = new PopEnum(str);
            pm.setNot("!".equals(m.group(1)));
            return pm;
        }
        // # - ^a.* : 弹出被正则表达式匹配的项目
        // # - !^a.* : 弹出没有被正则表达式匹配的项目
        p = Regex.getPattern("^(!)?(\\^.+)$");
        m = p.matcher(input);
        if (m.find()) {
            String str = Ws.trim(m.group(2));
            PopMatch pm = new PopRegex(str);
            pm.setNot("!".equals(m.group(1)));
            return pm;
        }
        // 默认采用 PopEquals
        return new PopEquals(input);
    }

}
