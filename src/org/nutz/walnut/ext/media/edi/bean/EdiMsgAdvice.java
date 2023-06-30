package org.nutz.walnut.ext.media.edi.bean;

// UNA:+.? '
public class EdiMsgAdvice {

    public char element;

    public char component;

    public char decimal;

    public char escaper;

    public char customized;

    public char segment;

    public EdiMsgAdvice() {}

    public EdiMsgAdvice(String input) {
        if (input.startsWith("UNA")) {
            char[] cs = input.substring(3).toCharArray();
            element = cs[0];
            component = cs[1];
            decimal = cs[2];
            escaper = cs[3];
            customized = cs[4];
            segment = cs[5];
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("UNA");
        sb.append(element);
        sb.append(component);
        sb.append(decimal);
        sb.append(escaper);
        sb.append(customized);
        sb.append(segment);
        return sb.toString();
    }
    
}
