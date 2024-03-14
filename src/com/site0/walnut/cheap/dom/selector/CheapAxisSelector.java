package com.site0.walnut.cheap.dom.selector;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.CheapMatcher;
import com.site0.walnut.cheap.dom.match.CheapAutoMatcher;
import com.site0.walnut.util.Ws;

import static com.site0.walnut.cheap.dom.selector.CheapSelectorType.*;

/**
 * 类选择器，是一条链。必须到达链的最低端（next==null）时，才算通过选择器
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class CheapAxisSelector implements CheapSelector {

    private CheapSelectorType type;

    private CheapAxisSelector next;

    private CheapMatcher matcher;

    private CheapAxisSelector() {
        this.type = ANY;
    }

    public CheapAxisSelector(String input) {
        this.type = ANY;
        this.valueOf(input);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        joinString(sb);
        return sb.toString();
    }

    public void joinString(StringBuilder sb) {
        if (CHILD == this.type) {
            sb.append(" > ");
        }
        sb.append(matcher.toString());
        if (null != next) {
            next.joinString(sb);
        }
    }

    public CheapSelector valueOf(String input) {
        input = Ws.trim(input);
        Pattern p = Pattern.compile("([\\s>]+)");
        Matcher m = p.matcher(input);
        int off = 0;
        CheapAxisSelector sel = this;
        while (m.find()) {
            int pos = m.start();
            String s = Ws.trim(m.group(1));

            // 前面有内容，那么就是一个匹配规则
            if (pos > off) {
                String str = Ws.trim(input.substring(off, pos));
                if (str.length() > 0) {
                    sel.matcher = new CheapAutoMatcher(str);
                    sel.next = new CheapAxisSelector();
                    sel = sel.next;
                }
            }

            // 指明子连接
            if (">".equals(s)) {
                sel.type = CHILD;
            }
            // 否则就是任意连接
            else {
                sel.type = ANY;
            }

            // 指向结束
            off = m.end();
        }

        // 处理最后一截
        if (off < input.length()) {
            String s = input.substring(off);
            sel.matcher = new CheapAutoMatcher(s);
        }

        // 返回自己
        return this;
    }

    @Override
    public int join(List<CheapElement> list, CheapElement el, int limit) {
        if (limit <= 0) {
            return 0;
        }
        int re = 0;
        CheapAxisSelector selector = this;
        // 匹配当前节点，进入下一层
        if (null == matcher || matcher.match(el)) {
            // 到底了就结束
            if (null == this.next) {
                list.add(el);
            }
            // 继续进入下一层
            else {
                selector = this.next;
            }
        }
        // 匹配不成功
        else if (CHILD == this.type) {
            selector = null;
        }

        // 没有更多选择器了
        if (null == selector) {
            return 0;
        }

        // 准备子节点，继续递归
        List<CheapElement> children = el.getChildElements();
        for (CheapElement child : children) {
            re += selector.join(list, child, limit - re);
            if (re >= limit) {
                break;
            }
        }
        return re;
    }

    /**
     * 判断任何一个节点是否匹配指定选择器
     * 
     * @param el
     *            传入 DOM 的任意一个节点
     * @return 节点是否能匹配选择器
     */
    public boolean match(CheapElement el) {
        List<CheapElement> ans = el.getAncestors();
        // 从头开始匹配
        return matchNodeChains(ans.iterator(), type);
    }

    private boolean matchNodeChains(Iterator<CheapElement> it, CheapSelectorType type) {
        // 得到当前的元素
        while (it.hasNext()) {
            CheapElement el = it.next();
            // 当前匹配成功，进入下一层
            if (matcher == null || matcher.match(el)) {
                // 到底了，就算成功了哦
                if (null == this.next) {
                    return true;
                }
                // 匹配下一层
                else {
                    return this.next.matchNodeChains(it, this.next.type);
                }
            }
            // 匹配失败，如果只能找 Children 那么就是失败了
            else if (CHILD == type) {
                return false;
            }
        }
        // 找不到
        return false;
    }

    public CheapSelectorType getType() {
        return type;
    }

    public void setType(CheapSelectorType type) {
        this.type = null == type ? ANY : type;
    }

    public CheapAxisSelector getNext() {
        return next;
    }

    public void setNext(CheapAxisSelector next) {
        this.next = next;
    }

    public CheapMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(CheapMatcher matcher) {
        this.matcher = matcher;
    }

}
