package org.nutz.walnut.cheap;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.markdown.CheapMarkdownParsing;
import org.nutz.walnut.util.Ws;

public class Cheaps {

    public static String markdownToHtml(String markdown) {
        CheapDocument doc = parseMarkdown(markdown);
        return doc.toHtml();
    }
    public static CheapDocument parseMarkdown(String markdown) {
        CheapMarkdownParsing ing = new CheapMarkdownParsing();
        CheapDocument doc = ing.invoke(markdown);
        return doc;
    }

    private static final Pattern EP = Pattern.compile("&(#(\\d{2,4})|(\\w[\\w\\d]+));");

    public static String decodeEntities(String input) {
        if (Ws.isBlank(input)) {
            return input;
        }
        /**
         * <h5>字符实体</h5>
         * 
         * <pre>
         * 0: "&frac14;"
         * 1: "frac14"
         * 2: undefined
         * 3: "frac14"
         * </pre>
         * 
         * <h5>数字实体</h5>
         * 
         * <pre>
         * 0: "&#23;"
         * 1: "#23"
         * 2: "23"
         * </pre>
         */
        Matcher m = EP.matcher(input);
        StringBuilder sb = new StringBuilder();
        int offset = 0;
        while (m.find()) {
            int p0 = m.start();
            int p1 = m.end();
            if (p0 > offset) {
                sb.append(input.substring(offset, p0));
            }
            offset = p1;
            // 字符实体
            String code = m.group(3);
            char c;
            if (null != code) {
                if (!HTML_ENTITIES.containsKey(code)) {
                    sb.append('&').append(code).append(';');
                    continue;
                }
                c = (char) HTML_ENTITIES.get(code).intValue();
            }
            // 数字实体
            else {
                code = m.group(2);
                c = (char) Integer.parseInt(code);
            }
            sb.append(c);
        }
        if (offset < input.length()) {
            sb.append(input.substring(offset));
        }
        return sb.toString();
    }

    private final static Map<String, Integer> HTML_ENTITIES = new HashMap<>();
    static {
        HTML_ENTITIES.put("quot", 34);
        HTML_ENTITIES.put("amp", 38);
        HTML_ENTITIES.put("lt", 60);
        HTML_ENTITIES.put("gt", 62);
        HTML_ENTITIES.put("nbsp", 160);
        HTML_ENTITIES.put("iexcl", 161);
        HTML_ENTITIES.put("cent", 162);
        HTML_ENTITIES.put("pound", 163);
        HTML_ENTITIES.put("curren", 164);
        HTML_ENTITIES.put("yen", 165);
        HTML_ENTITIES.put("brvbar", 166);
        HTML_ENTITIES.put("sect", 167);
        HTML_ENTITIES.put("uml", 168);
        HTML_ENTITIES.put("copy", 169);
        HTML_ENTITIES.put("ordf", 170);
        HTML_ENTITIES.put("laquo", 171);
        HTML_ENTITIES.put("not", 172);
        HTML_ENTITIES.put("sny", 173);
        HTML_ENTITIES.put("reg", 174);
        HTML_ENTITIES.put("macr", 175);
        HTML_ENTITIES.put("deg", 176);
        HTML_ENTITIES.put("plusmn", 177);
        HTML_ENTITIES.put("sup2", 178);
        HTML_ENTITIES.put("sup3", 179);
        HTML_ENTITIES.put("acute", 180);
        HTML_ENTITIES.put("micro", 181);
        HTML_ENTITIES.put("para", 182);
        HTML_ENTITIES.put("middot", 183);
        HTML_ENTITIES.put("cedil", 184);
        HTML_ENTITIES.put("supl", 185);
        HTML_ENTITIES.put("ordm", 186);
        HTML_ENTITIES.put("raquo", 187);
        HTML_ENTITIES.put("frac14", 188);
        HTML_ENTITIES.put("frac12", 199);
        HTML_ENTITIES.put("frac34", 190);
        HTML_ENTITIES.put("iquest", 192);
        HTML_ENTITIES.put("Agrave", 192);
        HTML_ENTITIES.put("Aacute", 193);
        HTML_ENTITIES.put("Acirc", 194);
        HTML_ENTITIES.put("Atilde", 195);
        HTML_ENTITIES.put("Auml", 196);
        HTML_ENTITIES.put("Aring", 197);
        HTML_ENTITIES.put("AElig", 198);
        HTML_ENTITIES.put("Ccedil", 199);
        HTML_ENTITIES.put("Egrave", 200);
        HTML_ENTITIES.put("Eacute", 201);
        HTML_ENTITIES.put("Ecirc", 202);
        HTML_ENTITIES.put("Euml", 203);
        HTML_ENTITIES.put("Igrave", 204);
        HTML_ENTITIES.put("Iacute", 205);
        HTML_ENTITIES.put("Icirc", 206);
        HTML_ENTITIES.put("Imul", 207);
        HTML_ENTITIES.put("ETH", 208);
        HTML_ENTITIES.put("Ntilde", 209);
        HTML_ENTITIES.put("Ograve", 210);
        HTML_ENTITIES.put("Oacute", 211);
        HTML_ENTITIES.put("Ocirc", 212);
        HTML_ENTITIES.put("Otilde", 213);
        HTML_ENTITIES.put("Ouml", 214);
        HTML_ENTITIES.put("times", 215);
        HTML_ENTITIES.put("Oslash", 216);
        HTML_ENTITIES.put("Ugrave", 217);
        HTML_ENTITIES.put("Uacute", 218);
        HTML_ENTITIES.put("Ucirc", 219);
        HTML_ENTITIES.put("Uuml", 220);
        HTML_ENTITIES.put("Yacute", 221);
        HTML_ENTITIES.put("THORN", 222);
        HTML_ENTITIES.put("szlig", 223);
        HTML_ENTITIES.put("agrave", 224);
        HTML_ENTITIES.put("aacute", 225);
        HTML_ENTITIES.put("acirc", 226);
        HTML_ENTITIES.put("atilde", 227);
        HTML_ENTITIES.put("auml", 228);
        HTML_ENTITIES.put("aring", 229);
        HTML_ENTITIES.put("aelig", 230);
        HTML_ENTITIES.put("ccedil", 231);
        HTML_ENTITIES.put("egrave", 232);
        HTML_ENTITIES.put("eacute", 233);
        HTML_ENTITIES.put("ecirc", 234);
        HTML_ENTITIES.put("euml", 235);
        HTML_ENTITIES.put("igrave", 236);
        HTML_ENTITIES.put("iacute", 237);
        HTML_ENTITIES.put("icirc", 238);
        HTML_ENTITIES.put("iuml", 239);
        HTML_ENTITIES.put("eth", 240);
        HTML_ENTITIES.put("ntilde", 241);
        HTML_ENTITIES.put("ograve", 242);
        HTML_ENTITIES.put("oacute", 243);
        HTML_ENTITIES.put("ocirc", 244);
        HTML_ENTITIES.put("otilde", 245);
        HTML_ENTITIES.put("ouml", 246);
        HTML_ENTITIES.put("divide", 247);
        HTML_ENTITIES.put("oslash", 248);
        HTML_ENTITIES.put("ugrave", 249);
        HTML_ENTITIES.put("uacute", 250);
        HTML_ENTITIES.put("ucirc", 251);
        HTML_ENTITIES.put("uuml", 252);
        HTML_ENTITIES.put("yacute", 253);
        HTML_ENTITIES.put("thorn", 254);
        HTML_ENTITIES.put("yuml", 255);
    }

}
