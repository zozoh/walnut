package org.nutz.walnut.cheap.dom;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheapText extends CheapRawData {

    protected CheapText() {
        this(null);
    }

    protected CheapText(String text) {
        this.type = CheapNodeType.TEXT;
        this.text = text;
        this.treeDisplayName = "!TEXT";
        this.treeDisplayLeftQuoteChar = '"';
        this.treeDisplayRightQuoteChar = '"';
    }

    @Override
    public void format(CheapFormatter cdf, int depth) {
        if (!this.isFormated() && this.isPlacehold()) {
            // 准备格式化文本
            if (null != this.next) {
                this.text = cdf.getPrefix(depth);
            }
            // 最后一个节点，需要回退一级缩进
            else {
                this.text = cdf.getPrefix(depth - 1);
            }
            // 标识格式化，以便幂等
            this.setFormatted(true);
        }
    }

    private static Pattern P = Pattern.compile("&([0-9a-z]+);");

    public String decodeText() {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        Matcher m = P.matcher(this.text);
        while (m.find()) {
            int s = m.start();
            // 前面
            if (s > pos) {
                sb.append(this.text.substring(pos, s));
            }
            // 找实体
            String enn = m.group(1);
            String c = entities.get(enn);
            if (null != c) {
                sb.append(c);
            } else {
                sb.append(m.group(0));
            }
            // 移动
            pos = m.end();
        }
        // 最后一段
        if (pos < this.text.length()) {
            sb.append(this.text.substring(pos));
        }
        // 搞定
        return sb.toString();
    }

    @Override
    public void joinString(StringBuilder sb) {
        sb.append(text);
    }

    private static Map<String, String> entities = new HashMap<>();

    static {
        entities.put("acute", "´");
        entities.put("copy", "©");
        entities.put("gt", ">");
        entities.put("micro", "µ");
        entities.put("reg", "®");
        entities.put("amp", "&");
        entities.put("deg", "°");
        entities.put("iexcl", "¡");
        entities.put("nbsp", " ");
        entities.put("raquo", "»");
        entities.put("brvbar", "¦");
        entities.put("divide", "÷");
        entities.put("iquest", "¿");
        entities.put("not", "¬");
        entities.put("sect", "§");
        entities.put("bull", "•");
        entities.put("frac12", "½");
        entities.put("laquo", "«");
        entities.put("para", "¶");
        entities.put("uml", "¨");
        entities.put("cedil", "¸");
        entities.put("frac14", "¼");
        entities.put("lt", "<");
        entities.put("plusmn", "±");
        entities.put("times", "×");
        entities.put("cent", "¢");
        entities.put("frac34", "¾");
        entities.put("macr", "¯");
        entities.put("quot", "“");
        entities.put("trade", "™");
        entities.put("euro", "€");
        entities.put("pound", "£");
        entities.put("yen", "¥");
        entities.put("bdquo", "„");
        entities.put("hellip", "…");
        entities.put("middot", "·");
        entities.put("rsaquo", "›");
        entities.put("ordf", "ª");
        entities.put("circ", "ˆ");
        entities.put("ldquo", "“");
        entities.put("mdash", "—");
        entities.put("rsquo", "’");
        entities.put("ordm", "º");
        entities.put("dagger", "†");
        entities.put("lsaquo", "‹");
        entities.put("ndash", "–");
        entities.put("sbquo", "‚");
        entities.put("rdquo", "”");
        entities.put("Dagger", "‡");
        entities.put("lsquo", "‘");
        entities.put("permil", "‰");
        entities.put("tilde", "˜");
        entities.put("asymp", "≈");
        entities.put("frasl", "⁄");
        entities.put("larr", "←");
        entities.put("part", "∂");
        entities.put("spades", "♠");
        entities.put("cap", "∩");
        entities.put("ge", "≥");
        entities.put("le", "≤");
        entities.put("Prime", "″");
        entities.put("sum", "∑");
        entities.put("clubs", "♣");
        entities.put("harr", "↔");
        entities.put("loz", "◊");
        entities.put("prime", "′");
        entities.put("uarr", "↑");
        entities.put("darr", "↓");
        entities.put("hearts", "♥");
        entities.put("minus", "−");
        entities.put("prod", "∏");
        entities.put("zwj", "‍");
        entities.put("diams", "♦");
        entities.put("infin", "∞");
        entities.put("ne", "≠");
        entities.put("radic", "√");
        entities.put("zwnj", "‌");
        entities.put("equiv", "≡");
        entities.put("int", "∫");
        entities.put("oline", "‾");
        entities.put("rarr", "→");
        entities.put("alpha", "α");
        entities.put("eta", "η");
        entities.put("mu", "μ");
        entities.put("pi", "π");
        entities.put("theta", "θ");
        entities.put("beta", "β");
        entities.put("gamma", "γ");
        entities.put("nu", "ν");
        entities.put("psi", "ψ");
        entities.put("upsilon", "υ");
        entities.put("chi", "χ");
        entities.put("iota", "ι");
        entities.put("omega", "ω");
        entities.put("rho", "ρ");
        entities.put("xi", "ξ");
        entities.put("delta", "δ");
        entities.put("kappa", "κ");
        entities.put("omicron", "ο");
        entities.put("sigma", "σ");
        entities.put("zeta", "ζ");
        entities.put("epsilon", "ε");
        entities.put("lambda", "λ");
        entities.put("phi", "φ");
        entities.put("tau", "τ");
        entities.put("Alpha", "Α");
        entities.put("Eta", "Η");
        entities.put("Mu", "Μ");
        entities.put("Pi", "Π");
        entities.put("Theta", "Θ");
        entities.put("Beta", "Β");
        entities.put("Gamma", "Γ");
        entities.put("Nu", "Ν");
        entities.put("Psi", "Ψ");
        entities.put("Upsilon", "Υ");
        entities.put("Chi", "Χ");
        entities.put("Iota", "Ι");
        entities.put("Omega", "Ω");
        entities.put("Rho", "Ρ");
        entities.put("Xi", "Ξ");
        entities.put("Delta", "Δ");
        entities.put("Kappa", "Κ");
        entities.put("Omicron", "Ο");
        entities.put("Sigma", "Σ");
        entities.put("Zeta", "Ζ");
        entities.put("Epsilon", "Ε");
        entities.put("Lambda", "Λ");
        entities.put("Phi", "Φ");
        entities.put("Tau", "Τ");
        entities.put("sigmaf", "ς");
    }

}
