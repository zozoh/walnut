package org.nutz.markdown;

public class Markdown {

    public static String toHtml(String str) {
        TransMarkdownToHtml t = new TransMarkdownToHtml();
        return t.trans(str);
    }

}
