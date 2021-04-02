package org.nutz.walnut.cheap.dom.match;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.Regex;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapMatcher;
import org.nutz.walnut.util.Ws;

public class CheapAutoMatcher implements CheapMatcher {

    private static String REGEX = "^" // Start: 1
                                  // TagBegin: 2,3(Name),4,5(Attrs)
                                  + "([\\w\\d:_-]+)?"
                                  // TagEnd: 6,7(name)
                                  + "(.([\\w\\d_-]+))?"
                                  // Comment begin: 8
                                  + "(\\[([\\w\\d:_-]+)(=(.+))?\\])?"
                                  + "$";
    private static Pattern P = Regex.getPattern(REGEX);

    private CheapMatcher ma;

    public CheapAutoMatcher(String str) {
        /**
         * <pre>
           0:[  0, 20) `w:p.abcdee[w:aa=ttt]`
           1:[  0,  3) `w:p`
           2:[  3, 10) `.abcdee`
           3:[  4, 10) `abcdee`
           4:[ 10, 20) `[w:aa=ttt]`
           5:[ 11, 15) `w:aa`
           6:[ 15, 19) `=ttt`
           7:[ 16, 19) `ttt`
         * </pre>
         */
        Matcher m = P.matcher(str);
        if (m.find()) {
            CheapAndMatcher cam = new CheapAndMatcher();
            String tagName = m.group(1);
            String className = m.group(3);
            String attrName = m.group(5);
            String attrValue = m.group(7);
            if (!Ws.isBlank(tagName)) {
                cam.addMatcher(new CheapTagNameMatcher(tagName));
            }
            if (!Ws.isBlank(className)) {
                cam.addMatcher(new CheapClassNameMatcher(className));
            }
            if (!Ws.isBlank(attrName)) {
                cam.addMatcher(new CheapAttrMatcher(attrName, attrValue));
            }
            ma = cam;
        }
        // Attr: [xxx]
        else if (Ws.isQuoteBy(str, '[', ']')) {
            ma = new CheapAttrMatcher(str);
        }
        // Class: .xxx
        else if (str.startsWith(".")) {
            ma = new CheapClassNameMatcher(str);
        }
        // TagName
        else if (str.startsWith("^")) {
            ma = new CheapRegexTagNameMatcher(str);
        }
        // TagName
        else {
            ma = new CheapTagNameMatcher(str);
        }
    }

    @Override
    public boolean match(CheapElement el) {
        if (null != ma) {
            return ma.match(el);
        }
        return false;
    }

}
