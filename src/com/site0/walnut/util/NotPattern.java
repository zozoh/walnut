package com.site0.walnut.util;

import java.util.regex.Pattern;

public class NotPattern {

    public Pattern regex;

    public NotPattern(String regex) {
        this.regex = Wregex.getPattern(regex);
    }

    public boolean match(String s) {
        return !regex.matcher(s).find();
    }

}
