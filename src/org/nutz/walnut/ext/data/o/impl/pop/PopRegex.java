package org.nutz.walnut.ext.data.o.impl.pop;

import java.util.regex.Pattern;

/**
 * <ul>
 * <li><code>^a.*</code> : 弹出被正则表达式匹配的项目
 * <li><code>!^a.*</code> : 弹出没有被正则表达式匹配的项目
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class PopRegex extends PopMatch {

    private Pattern ptn;

    public PopRegex(String input) {
        this.ptn = Pattern.compile(input);
    }

    @Override
    protected boolean isMatch(Object ele) {
        if (null != ele) {
            String s = ele.toString();
            return ptn.matcher(s).find();
        }
        return false;
    }

}
