package org.nutz.walnut.impl.box.weixin;

import java.util.Collection;
import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.weixin.bean.WxInMsg;

public class WxMsgHandler {

    public Object match;

    public boolean context;

    public Object command;

    @SuppressWarnings("unchecked")
    public boolean isMatched(WxInMsg im) {
        if (null != match) {
            // 仅仅一个判断条件
            if (match instanceof Map<?, ?>) {
                return _is_matched(im, (Map<String, Object>) match);
            }
            // 很多判断条件
            else if (match instanceof Collection<?>) {
                for (Object ele : (Collection<?>) match) {
                    // 选择
                    if (ele instanceof Map<?, ?>) {
                        if (_is_matched(im, (Map<String, Object>) ele))
                            return true;
                    }
                    // 字符串
                    else if (ele.toString().equals(im.getContent())) {
                        return true;
                    }
                }
                return false;
            }
            // 其他的，当做文本处理
            else {
                return match.toString().equals(im.getContent());
            }
        }
        return true;
    }

    private boolean _is_matched(WxInMsg im, Map<String, Object> map) {
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            // MsgType
            if ("MsgType".equals(key)) {
                if (!_do_match(val, im.getMsgType()))
                    return false;
            }
            // Content
            else if ("Content".equals(key)) {
                if (!_do_match(val, im.getContent()))
                    return false;
            }
            // Event
            else if ("Event".equals(key)) {
                if (!_do_match(val, im.getEvent()))
                    return false;
            }
            // EventKey
            else if ("EventKey".equals(key)) {
                if (!_do_match(val, im.getEventKey()))
                    return false;
            }
            // 不可能
            else {
                throw Er.create("e.cmd.weixin.invalid.matchkey", key);
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean _do_match(Object pattern, String str) {
        // 正则表达式
        if (pattern instanceof Map) {
            String regex = ((Map<String, String>) pattern).get("regex");
            return str.matches(regex);
        }
        // 普通字符串
        String exp = Strings.trim(pattern.toString());
        return exp.equalsIgnoreCase(Strings.trim(str));
    }

}
