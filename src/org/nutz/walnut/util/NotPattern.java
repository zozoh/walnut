package org.nutz.walnut.util;

import java.util.regex.Pattern;

import org.nutz.lang.util.Regex;

public class NotPattern {

    public Pattern regex;

    public NotPattern(String regex) {
        this.regex = Regex.getPattern(regex);
    }

    public boolean match(String s) {
        return !regex.matcher(s).find();
    }

}
