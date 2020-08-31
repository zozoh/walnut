package org.nutz.walnut.validate.match;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.Regex;
import org.nutz.walnut.validate.WnMatch;

public class RegexMatch implements WnMatch {

    private Pattern ptn;

    private boolean not;

    public RegexMatch(Pattern ptn, boolean not) {
        this.ptn = ptn;
        this.not = not;
    }

    public RegexMatch(String regex) {
        if (regex.startsWith("!")) {
            this.not = true;
            this.ptn = Regex.getPattern(regex.substring(1));
        } else {
            this.not = false;
            this.ptn = Regex.getPattern(regex);
        }
    }

    @Override
    public boolean match(Object val) {
        if (null == val) {
            return false;
        }
        Matcher m = ptn.matcher(val.toString());
        return m.find() ^ not;
    }

}
