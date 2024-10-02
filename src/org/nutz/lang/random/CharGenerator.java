package org.nutz.lang.random;

/**
 * Generted one char
 * 
 * @author zozoh
 * @author wendal(wendal1985@gmail.com)
 */
public class CharGenerator {

    private static final char[] src_62 = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] src_36 = "1234567890abcdefghijklmnopqrstuvwxyz".toCharArray();

    private char[] cs;

    public CharGenerator() {
        this(src_62);
    }

    public CharGenerator(char[] cs) {
        this.cs = cs;
    }

    public CharGenerator useSrc36() {
        this.cs = src_36;
        return this;
    }

    public CharGenerator useSrc62() {
        this.cs = src_62;
        return this;
    }

    public char nextChar() {
        return cs[Math.abs(R.r.nextInt(cs.length))];
    }

    private static CharGenerator CG_62 = new CharGenerator().useSrc62();
    private static CharGenerator CG_36 = new CharGenerator().useSrc36();

    public static CharGenerator me() {
        return CG_62;
    }

    public static CharGenerator me36() {
        return CG_36;
    }

    public static char next() {
        return CG_62.nextChar();
    }

    public static char next36() {
        return CG_36.nextChar();
    }
}
