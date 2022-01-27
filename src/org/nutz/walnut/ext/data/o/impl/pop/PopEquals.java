package org.nutz.walnut.ext.data.o.impl.pop;

/**
 * <ul>
 * <li><code>=xyz</code> : 弹出内容为 'xyz' 的项目
 * <li><code>!=xyz</code> : 弹出内容不为 'xyz' 的项目
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class PopEquals extends PopMatch {

    private Object val;

    public PopEquals(Object input) {
        this.val = input;
    }

    @Override
    protected boolean isMatch(Object ele) {
        return null != ele && ele.equals(val);
    }

}
