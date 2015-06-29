package org.nutz.walnut.util;

import java.util.regex.Pattern;

public class NotPattern {

    public Pattern regex;

    public NotPattern(String regex) {
        this.regex = Pattern.compile(regex);
    }

    public boolean match(String s) {
        return !regex.matcher(s).find();
    }

}
