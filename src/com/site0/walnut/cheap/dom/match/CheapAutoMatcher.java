package com.site0.walnut.cheap.dom.match;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.Regex;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.CheapMatcher;
import com.site0.walnut.util.Ws;

public class CheapAutoMatcher implements CheapMatcher {

    private static String REGEX = "(" // Start: 1
                                  // Tag: 2
                                  + "([^.\\[]+)"
                                  // Class: 3
                                  + "|(\\.[^.\\[]+)"
                                  // Attribute: 4
                                  + "|(\\[[^.\\[]+\\])"
                                  + ")";
    private static Pattern P = Regex.getPattern(REGEX);

    private CheapMatcher ma;

    public CheapAutoMatcher(String str) {
        if (str.startsWith("^")) {
            ma = new CheapRegexTagNameMatcher(str);
        }
        /**
         * <pre>
            0/3  Regin:0/21
             0:[  0,  3) `pre`
             1:[  0,  3) `pre`
             2:[  0,  3) `pre`
             3:[ -1, -1) `null`
             4:[ -1, -1) `null`
            
            3/7  Regin:0/21
             0:[  3,  7) `.abc`
             1:[  3,  7) `.abc`
             2:[ -1, -1) `null`
             3:[  3,  7) `.abc`
             4:[ -1, -1) `null`
            
            7/14  Regin:0/21
             0:[  7, 14) `[xx=12]`
             1:[  7, 14) `[xx=12]`
             2:[ -1, -1) `null`
             3:[ -1, -1) `null`
             4:[  7, 14) `[xx=12]`
         * </pre>
         */
        else {
            CheapAndMatcher cam = new CheapAndMatcher();
            Matcher m = P.matcher(str);
            while (m.find()) {
                String tagName = m.group(2);
                String className = m.group(3);
                String attr = m.group(4);
                // 标签名
                if (!Ws.isBlank(tagName)) {
                    cam.addMatcher(new CheapTagNameMatcher(tagName));
                }
                // 类选择器
                else if (!Ws.isBlank(className)) {
                    cam.addMatcher(new CheapClassNameMatcher(className));
                }
                // 属性选择器
                else if (!Ws.isBlank(attr)) {
                    cam.addMatcher(new CheapAttrMatcher(attr));
                }
            }
            if (cam.list.size() == 1) {
                ma = cam.list.get(0);
            } else {
                ma = cam;
            }
        }
    }

    public String toString() {
        if (null == ma) {
            return null;
        }
        return ma.toString();
    }

    @Override
    public boolean match(CheapElement el) {
        if (null != ma) {
            return ma.match(el);
        }
        return false;
    }

}
