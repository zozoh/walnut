package com.site0.walnut.ext.net.weixin;

import java.util.Collection;
import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.lang.util.Region;
import com.site0.walnut.api.err.Er;
import org.nutz.weixin.bean.WxInMsg;
import org.nutz.weixin.bean.WxScanCodeInfo;
import org.nutz.weixin.bean.WxSendLocationInfo;

public class WxMsgHandler {

    public String id;

    public Object match;

    public boolean context;

    public Object command;

    @SuppressWarnings("unchecked")
    public boolean isMatched(WxInMsg im) {
        if (null != match) {
            // 仅仅一个判断条件
            if (match instanceof Map<?, ?>) {
                Map<String, Object> map = (Map<String, Object>) match;
                return __match_by_map(im, map);
            }
            // 很多判断条件
            else if (match instanceof Collection<?>) {
                for (Object ele : (Collection<?>) match) {
                    // 选择
                    if (ele instanceof Map<?, ?>) {
                        if (__match_by_map(im, (Map<String, Object>) ele))
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
                return this.__match_text(match, im.getContent());
            }
        }
        return true;
    }

    private boolean __match_double_region(Object val, double nb) {
        Region<Double> rg = Region.Double(val.toString());
        return rg.match(nb);
    }

    @SuppressWarnings("unchecked")
    private boolean __match_text(Object pattern, String str) {
        // 空字符串，啥也甭想了，返回 false 吧
        if (null == str)
            return false;

        // 用 {regex:"xxx"} 表示的正则表达式
        if (pattern instanceof Map) {
            String regex = ((Map<String, String>) pattern).get("regex");
            if (null != regex) {
                return str.matches(regex);
            }
        }
        // 得到字符串
        String exp = Strings.trim(pattern.toString());

        // ^开头表示正则表达式
        if (exp.startsWith("^") && exp.length() > 1) {
            return str.matches(exp);
        }

        // 字符串精确匹配，但是忽略大小写
        return exp.equalsIgnoreCase(Strings.trim(str));
    }

    private boolean __match_by_map(WxInMsg im, Map<String, Object> map) {

        // Map 仅仅是个 {regex:"xxxx"}
        if (map.containsKey("regex")) {
            return this.__match_text(map, im.getContent());
        }

        // 详细判断
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            // MsgType
            if ("MsgType".equals(key)) {
                if (!__match_text(val, im.getMsgType()))
                    return false;
            }
            // Content
            else if ("Content".equals(key)) {
                if (!__match_text(val, im.getContent()))
                    return false;
            }
            // Event
            else if ("Event".equals(key)) {
                if (!__match_text(val, im.getEvent()))
                    return false;
            }
            // EventKey
            else if ("EventKey".equals(key)) {
                if (!__match_text(val, im.getEventKey()))
                    return false;
            }
            // ScanCodeInfo
            else if (key.startsWith("ScanCodeInfo.")) {
                String subKey = key.substring("ScanCodeInfo.".length());
                WxScanCodeInfo scinfo = im.getScanCodeInfo();
                // ScanCodeInfo.ScanType
                if ("ScanType".equals(subKey)) {
                    if (!__match_text(val, scinfo.getScanType()))
                        return false;
                }
                // ScanCodeInfo
                else if ("ScanResult".equals(subKey)) {
                    if (!__match_text(val, scinfo.getScanResult()))
                        return false;
                }
                // 错误的键
                else {
                    throw Er.create("e.cmd.weixin.invalid.matchkey.ScanCodeInfo", subKey);
                }
            }
            // SendLocationInfo
            else if (key.startsWith("SendLocationInfo.")) {
                String subKey = key.substring("SendLocationInfo.".length());
                WxSendLocationInfo slinfo = im.getSendLocationInfo();
                // Label.ScanType
                if ("Label".equals(subKey)) {
                    if (!__match_text(val, slinfo.getLabel()))
                        return false;
                }
                // ScanCodeInfo
                else if ("Poiname".equals(subKey)) {
                    if (!__match_text(val, slinfo.getPoiname()))
                        return false;
                }
                // Location_X
                else if ("Location_X".equals(subKey)) {
                    if (!__match_double_region(val, slinfo.getLocation_X()))
                        return false;
                }
                // Location_Y
                else if ("Location_Y".equals(subKey)) {
                    if (!__match_double_region(val, slinfo.getLocation_Y()))
                        return false;
                }
                // Scale
                else if ("Scale".equals(subKey)) {
                    if (!__match_double_region(val, slinfo.getScale()))
                        return false;
                }
                // 错误的键
                else {
                    throw Er.create("e.cmd.weixin.invalid.matchkey.SendLocationInfo", subKey);
                }
            }
            // 不可能
            else {
                throw Er.create("e.cmd.weixin.invalid.matchkey", key);
            }
        }
        return true;
    }

}
